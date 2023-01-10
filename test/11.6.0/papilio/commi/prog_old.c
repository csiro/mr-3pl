#include    <stdio.h>       
#include    <stdlib.h>
#include    <sys/file.h>
#include    <sys/types.h>
#include    <sys/stat.h>
#include    <stdio.h>
#include    <math.h>
#include    <unistd.h>
#include    <fcntl.h>
#include    <sys/uio.h>
#include    <termios.h>
#include    <signal.h>
#include    <fts.h>
#include    <errno.h>
#include    <string.h>
#include    <sched.h>
#include    <pthread.h>
#include    <semaphore.h>
#include    <sys/wait.h>



#define RVAL    0
#define RMEM    1
#define WVAL    2
#define WMEM    3

#define BSIZE   1024

sem_t*          sem[16];
char            sem_name[16][6];

//static char     usbsearch[] = "tty.usbmodem";   // for AVnet board on OSX
static char usbsearch[] = "tty.usbserial";   // for papilio board on OSX
static int      usbsearchlen = 12;
static char     dev[6] = "/dev/";
char            portName[200];
int             fd;
struct termios  tio;
static int      pipefd[2];
static char     buffer[BSIZE];


void        comms_write (
                int         mem,        // memory write 1, static or queue write 0
                uint8_t     address,    // FPGA register address
                uint16_t    memaddress, // memory address if required
                int         bytes,      // number of data bytes
                char*       ptr         // pointer to data
            );
void        comms_read (
                int         mem,        // memory read 1, value or queue read 0
                uint8_t     address,    // FPGA register address
                uint16_t    memaddress, // memory address if required
                int         bytes,      // number of data bytes to be received
                int         signed,     // 1 if signed, 0 if unsigned
                int         fill,       // number of top bytes to fill
                char*       ptr         // pointer to data destination
            );

void        write_ (
                char*   ptr,
                int     bytes
            );
int         read_ (
                char*   ptr,
                int     bytes
            );
void*       input_handler ();
int         compare (const FTSENT**, const FTSENT**);
void        findserialdev();
int         open_port ();
void        close_port ();
pthread_t   thread_start(
                void *  (*func)(void*),
                void *  arg,
                char *  name,
                int     priority
            );
void*       int_handler0 ();
void*       int_handler1 ();
void*       int_handler2 ();
void*       int_handler3 ();

#include    "prog.h"

int
main (
    int     argc,
    char**  argv
) {
    pthread_t           tideh, tide0, tide1, tide2, tide3;
    int                 pri_min = 1;
    uint16_t            wdata16 = 0x5536;
    uint16_t            rdata16;
    int64_t             wdata64 = 0x57364925;
    int64_t             rdata64;
    uint8_t             wdata8 = 0x34;
    uint8_t             rdata8;
    int16_t             wqdata16 = -2650;
    int16_t             rqdata16;
    uint16_t            wmdata16 = 1234;
    uint16_t            rmdata16;
    int                 r;
    int16_t             rqavail;
    
    
        

    fd = open_port();
    
    pipe(pipefd);
    
    for (int i=0 ; i<16 ; i++) {
        sprintf(sem_name[i], "/e%d", i);
        sem_unlink(sem_name[i]);    // remove if already exists!
        if ((sem[i] = sem_open(sem_name[i], O_CREAT, 0600, 0)) == SEM_FAILED) {
            perror("sem_open");
            exit(1);
        }
    }
    tideh = thread_start(input_handler, NULL, "input_handler", pri_min);
    tide0 = thread_start(int_handler0, NULL, "int_handler0", pri_min);
    tide1 = thread_start(int_handler1, NULL, "int_handler1", pri_min);
    tide2 = thread_start(int_handler2, NULL, "int_handler2", pri_min);
    tide3 = thread_start(int_handler3, NULL, "int_handler3", pri_min);




    
    rdata16 = fpga_R_read();
    printf("read %d 0x%x (should be 0x137 first time)\n", rdata16, rdata16);
    
    fpga_W_write(wdata16);
    rdata16 = fpga_R_read();
    printf("read %d 0x%x (should be %d 0x%x)\n", rdata16, rdata16, wdata16, wdata16);
    
    fpga_W_write(wdata16);
    rdata16 = fpga_R_read();
    printf("read %d 0x%x (should be %d 0x%x)\n", rdata16, rdata16, wdata16, wdata16);
    
    fpga_WW_write(wdata64);
    rdata64 = fpga_RR_read();
    printf("0x%llx (should be 0x%llx)\n", rdata64, wdata64);
    
    fpga_WW_write(-137);
    rdata64 = fpga_RR_read();
    printf("%lld (should be -137)\n", rdata64);

    printf("should get interrupt EVR\n");
    fpga_WWW_write(wdata8);
    rdata8 = fpga_RRR_read();
    printf("0x%x (should be 0x%x)\n", rdata8, wdata8);

    fpga_RQ_avail_thresh_write(2);
    fpga_WQ_avail_thresh_write(14);
    
    rqavail = fpga_RQ_avail_read();
    printf("q avail %d (should be 0)\n", rqavail);
    rqdata16 = fpga_RQ_read();
    printf("q read %d 0x%x (should be -1 0xffff)\n", rqdata16, rqdata16);

    printf("should get interrupt EVQ\n");
    fpga_WQ_write(wqdata16);

    printf("should get interrupt EVQ\n");
    printf("should get interrupt RQ\n");
    fpga_WQ_write(wqdata16);
    rqavail = fpga_RQ_avail_read();
    printf("q avail %d (should be 2)\n", rqavail);

    printf("should get interrupt EVQ\n");
    fpga_WQ_write(wqdata16);
    rqavail = fpga_RQ_avail_read();

    rqavail = fpga_RQ_avail_read();
    printf("q avail %d (should be 3)\n", rqavail);
    rqdata16 = fpga_RQ_read();
    printf("q read %d 0x%x (should be %d 0x%x)\n", rqdata16, rqdata16, wqdata16, wqdata16);

    rqavail = fpga_RQ_avail_read();
    printf("q avail %d (should be 2)\n", rqavail);
    printf("should get interrupt WQ\n");
    rqdata16 = fpga_RQ_read();
    printf("q read %d 0x%x (should be %d 0x%x)\n", rqdata16, rqdata16, wqdata16, wqdata16);

    rqavail = fpga_RQ_avail_read();
    printf("q avail %d (should be 1)\n", rqavail);
    rqdata16 = fpga_RQ_read();
    printf("q read %d 0x%x (should be %d 0x%x)\n", rqdata16, rqdata16, wqdata16, wqdata16);
    rdata16 = fpga_R_read();

    rqavail = fpga_RQ_avail_read();
    printf("q avail %d (should be 0)\n", rqavail);
    rqdata16 = fpga_RQ_read();
    printf("q read %d 0x%x (should be -1 0xffff)\n", rqdata16, rqdata16);

    rmdata16 = fpga_MR_read(10);
    printf("read %d (should be 30)\n", rmdata16);
    rmdata16 = fpga_MR_read(20);
    printf("read %d (should be 60)\n", rmdata16);

    fpga_MW_write(wmdata16, 15);
    rmdata16 = fpga_MR_read(15);
    printf("read %d 0x%x (should be %d 0x%x)\n", rmdata16, rmdata16, wmdata16, wmdata16);




    
    pthread_cancel(tideh);
    pthread_cancel(tide0);
    pthread_cancel(tide1);
    pthread_cancel(tide2);
    pthread_cancel(tide3);
    for (int i=0 ; i<16 ; i++) {
        sem_close(sem[i]);
        sem_unlink(sem_name[i]);
    }
    close_port();
    close(pipefd[0]);
    close(pipefd[1]);
    
    exit(0);
}

void*
int_handler0 () {
    for(;;) {
        fpga_EVQ_wait();
        printf("interrupt EVQ\n");
    }
}

void*
int_handler1 () {
    for (;;) {
        fpga_EVR_wait();
        printf("interrupt EVR\n");
    }
}

void*
int_handler2 () {
    for(;;) {
        fpga_RQ_wait();
        printf("interrupt RQ\n");
    }
}

void*
int_handler3 () {
    for (;;) {
        fpga_WQ_wait();
        printf("interrupt WQ\n");
    }
}

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
    
    write_(&com, 1); // send command byte
    if (mem != 0)
        write_((char*)&memaddress, 2); // send memory address
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

void*
input_handler () {
    int     r;
    char    c;
    int     n;
    int     bytes;
    char*   ptr;
    
    for (;;) {
        r = read(fd, &c, 1);
        if (r < 0) {
            printf("uart read error\n");
            exit(1);
        }
        n = c & 0x7f;   // byte count or interrupt ID
        if (c & 0x80) {
            // is an interrupt
            if (n >= 16)
                printf("interrupt %d illegal (max 16) - ignored\n", n);
            else
                sem_post(sem[n]);
        } else {
            // is a command return -
            // read the data
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

// Search for a serial port.
// 'usbsearch' gives the head of the device name and 'usbsearchlen' is the
// length of string to search.
// IF MORE THAN ONE PORT MATCHES THE LAST IS USED.
void
findserialdev() {
    FTS*    file_system = NULL;
    FTSENT* child = NULL;
    FTSENT* parent = NULL;
    char*   pathlist[2] = {"/dev", NULL};

    file_system = fts_open(pathlist, FTS_COMFOLLOW | FTS_NOCHDIR, &compare);

    if (NULL != file_system) {
        while( (parent = fts_read(file_system)) != NULL) {
            child = fts_children(file_system,0);

            if (errno != 0)
                perror("fts_children");
            while ((NULL != child) && (NULL != child->fts_link)) {
                child = child->fts_link;
                if (strncmp(child->fts_name, usbsearch, usbsearchlen) == 0) {
                    strcpy(portName, dev);
                    strcat(portName, child->fts_name);
                    printf("found port = %s\n", portName);
                }
            }
        }
        fts_close(file_system);
    }
}

int compare(const FTSENT** one, const FTSENT** two) {
    return (strcmp((*one)->fts_name, (*two)->fts_name));
}

int
open_port () {
    int             fd;
    struct termios  ntio;
    int             flags;

    findserialdev();
    if (strlen(portName) == 0) {
        fprintf(stderr, "could not find USB serial device");
        exit(1);
    }
    
    fd = open(portName, O_RDWR | O_NOCTTY | O_NONBLOCK);
    if (fd < 0) {
        fprintf(stderr, "Cannot open USB serial device\n");
        exit(1);
    }
    printf("port %s open\n", portName);
    
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
    void *  arg,
    char *  name,
    int     priority
) {
    int                 status;
    struct sched_param  sp;
    pthread_t           tid;

    if ((status = pthread_create(&tid, NULL, func, arg)) != 0) {
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
