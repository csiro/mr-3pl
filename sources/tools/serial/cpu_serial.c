#include    "cpu_serial.h"

#define BSIZE   1024

sem_t**         sem;
char**          sem_name;
char            buffer[BSIZE];
int             fd;
struct termios  tio;
static int      pipefd[2];
pthread_t       tideh;
int             ninterrupts;
char*           dma_addr;
    
void        set_dma_address (char* addr);
int         open_port (char* pname);
void        close_port ();
void        write_ (
                char*   ptr,
                int     bytes
            );
int         read_ (
                char*   ptr,
                int     bytes
            );
void*       input_handler ();

// Open the serial interface to the FPGA.
// pname is the serial device, e.g. "/dev/tty.modem12345"
void
fpga_open(char* pname) {
    ninterrupts = fpga_interrupts;
    fd = open_port(pname);
    pipe(pipefd);
    if (ninterrupts != 0) {
        sem = (sem_t**)malloc(ninterrupts * sizeof(sem_t*));
        sem_name = (char**)malloc(ninterrupts * sizeof(char*));
        for (int i=0 ; i<ninterrupts ; i++) {
            sem_name[i] = (char*)malloc(6);
            sprintf(sem_name[i], "/e%d", i);
            sem_unlink(sem_name[i]);    // remove if already exists!
            if ((sem[i] = sem_open(sem_name[i], O_CREAT, 0600, 0)) == SEM_FAILED) {
                perror("sem_open");
                exit(1);
            }
        }
    }
    tideh = thread_start(input_handler, "input_handler", 1);
}

// Close the serial interface to the FPGA.
void
fpga_close() {
    pthread_cancel(tideh);
    for (int i=0 ; i<ninterrupts ; i++) {
        sem_close(sem[i]);
        sem_unlink(sem_name[i]);
    }
    close_port();
    close(pipefd[0]);
    close(pipefd[1]);
}

// Set the DMA base address.
// DMA addresses in the FPGA are relative to this base address.
void
set_dma_address (char* addr) {
    dma_addr = addr;
}

// Write a command to the FPGA.
void
comms_write (
    int         mem,        // memory write 1, static or queue write 0
    uint8_t     address,    // FPGA register address
    uint16_t    memaddress, // memory address if required
    int         bytes,      // number of data bytes
    char*       ptr         // pointer to data
) {
    char    com = 0x80 | (mem << 6) | address;
    int     i;
    int     n = bytes;
    char*   pp = ptr;
    
    write_(&com, 1); // send command byte
    if (mem != 0)
        write_((char*)&memaddress, 2); // send memory address
    n = bytes;
    while (n > 0) {
        i = write(fd, ptr, n);
        n -= i;
        ptr += i;
    }
}

void
write_ (
    char*   ptr,
    int     bytes
) {
    int i;
    
    if (write(fd, ptr, bytes) < 0) {
        fprintf(stderr, "data write error\n");
        exit(1);
    }
}

// Read returned data from the FPGA.
void
comms_read (
    int         mem,        // memory read 1, value or queue read 0
    uint8_t     address,    // FPGA register address
    uint16_t    memaddress, // memory address if required
    int         bytes,      // number of data bytes to be received
    int         sig,        // 1 if signed, 0 if unsigned
    int         fill,       // number of top bytes to fill
    char*       ptr         // pointer to data destination
) {
    char    com = (mem << 6) | address; // set address and memory flag in command
    int     n = bytes;  // byte counter initialisation
    char    snap;       // most significant byte
    char    pad = 0;    // padding byte - 0 or 0xff
    int     i;

    // send command byte
    write_(&com, 1);
    
    // send memory address if is a memory read
    if (mem != 0)
        write_((char*)&memaddress, 2);
    
    // receive data and write to destination address
    while (n > 0) {
        i = read_(ptr, n);
        n -= i;
        ptr += i;
    }
    snap = *(ptr - 1);
    
    // if whole result type has been filled, return
    if (fill == 0)
        return;
    
    // fill remainder of result with 0x00 (unsigned or +ve) or 0xff (signed and -ve)
    if ((sig != 0) && ((snap & 0x80) != 0))
        pad = 0xff;    // 0xff if signed and -ve
    while (fill-- != 0)
        *ptr++ = pad;   // fill most significant bytes
}

int
read_ (
    char*   ptr,
    int     bytes
) {
    int r;
    r = read(pipefd[0], ptr, bytes);
    if (r < 0) {
        fprintf(stderr, "data read error\n");
        exit(1);
    }
    return(r);
}

// Handle data read from the FPGA.
// this may be -
//      data returned from an FPGA read
//      an interrupt request from the FPGA
//      a DMA request from the FPGA
void*
input_handler () {
    int         r;
    char        c;
    int         n;
    int         bytes;
    uint16_t    addr;
    char*       ptr;
    uint16_t    abuf;
    
    for (;;) {
        r = read(fd, &c, 1);
        if (r < 0) {
            printf("uart read error\n");
            exit(1);
        }
        if (c & 0x80) {
            switch (c & 0x60) {
            case 0x00:
                // is an interrupt
                n = c & 0x0f;   // interrupt ID
                if (n >= ninterrupts)
                    printf("interrupt %d illegal (max %d) - ignored\n", n, ninterrupts);
                else
                    sem_post(sem[n]);
                break;
            case 0x20:
                // is FPGA -> CPU DMA
                bytes = c & 0x1f;   // byte count
                read(fd, &addr, 2);
                ptr = dma_addr + addr;
                while (bytes != 0) {
                    read(fd, ptr, 1);
                    bytes--;
                    ptr++;
                }
                break;
            case 0x40:
                // is CPU -> FPGA DMA
                // Execute a write to a queue at address 0.
                bytes = c & 0x1f;   // byte count
                read(fd, &addr, 2);
                ptr = dma_addr + addr;                
                comms_write (0, 0, 0, bytes, ptr);
            }
        } else {
            // is a command return -
            // read the data
            n = c & 0x7f;   // byte count
            bytes = n;
            ptr = buffer;
            while (bytes > 0) {
                r = read(fd, ptr, bytes);
                bytes -= r;
                ptr += r;
            }
            // write the data back to the command requesting it
            bytes = n;
            ptr = buffer;
            while (bytes > 0) {
                r = write(pipefd[1], ptr, bytes);
                bytes -= r;
                ptr += r;
            }
        }
    }
}

// Open the serial port.
int
open_port (char* pname) {
    int             fd;
    struct termios  ntio;
    int             flags;

    if (strlen(pname) == 0) {
        fprintf(stderr, "could not find USB serial device");
        exit(1);
    }
    
    fd = open(pname, O_RDWR | O_NOCTTY | O_NONBLOCK);
    if (fd < 0) {
        fprintf(stderr, "Cannot open USB serial device\n");
        exit(1);
    }
    printf("port %s open\n", pname);
    
    // Make the file descriptor blocking.
    flags = fcntl(fd, F_GETFL);
    flags &= ~O_NONBLOCK;
    fcntl(fd, F_SETFL, flags);


    tcgetattr(fd, &tio); // get current port settings
    bzero(&ntio,sizeof(ntio)); // clear struct for new port settings
    ntio.c_cflag = CS8 | CREAD | CSTOPB | CLOCAL;
    ntio.c_iflag = IGNPAR;
    ntio.c_oflag = 0;       // OCANON
    ntio.c_lflag = 0;       // ICANON
    ntio.c_cc[VMIN] = 1;    // blocking read until 1 character arrives
    ntio.c_cc[VSTART] = _POSIX_VDISABLE;
    ntio.c_cc[VSTOP] = _POSIX_VDISABLE;
    ntio.c_cc[VINTR] = _POSIX_VDISABLE;
    ntio.c_cc[VQUIT] = _POSIX_VDISABLE;
    ntio.c_cc[VSUSP] = _POSIX_VDISABLE;
    cfsetispeed(&ntio, B115200);
    cfsetospeed(&ntio, B115200);

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW, &ntio);
    
    return(fd);
}

// Close the serial port.
void
close_port () {
    // Restore terminal settings
    tcsetattr(fd, TCSANOW, &tio);
    close(fd);
}

// Start a thread.
pthread_t
thread_start(
    void *  (*func)(void*),
    char *  name,
    int     priority
) {
    int                 status;
    struct sched_param  sp;
    pthread_t           tid;

    if ((status = pthread_create(&tid, NULL, func, NULL)) != 0) {
	printf("thread_start(): pthread_create() failed thread '%s' (err = %s)\n",
	    name, strerror(status));
        exit(1);
    }

    sp.sched_priority = priority;

    status = pthread_setschedparam(tid, SCHED_RR, &sp);
    if (status != 0) {
    	printf("thread_start(): pthread_setschedparam() failed for thread '%s' (err = %s)\n",
	    name, strerror(status));
        exit(1);
    }
    return(tid);
}
