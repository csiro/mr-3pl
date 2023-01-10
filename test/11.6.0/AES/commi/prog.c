#include    "cpu_serial.h"
#include    <fts.h>
#include    <math.h>



static char     usbsearch[] = "tty.usbmodem";   // for AVnet board on OSX
//static char     usbsearch[] = "tty.usbserial";   // for papilio board on OSX
static int      usbsearchlen = 12;
static char     dev[6] = "/dev/";
char            portName[200];

int         compare (const FTSENT**, const FTSENT**);
void        findserialdev ();
void*       WQ_handler ();
void*       RQ_handler ();
void*       EVR_handler ();
void*       EVQ_handler ();

#include    "prog.h"

int
main (
    int     argc,
    char**  argv
) {
    pthread_t           WQ_thread, RQ_thread, EVR_thread, EVQ_thread;
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
    
    
    // Find the interface serial device.
    findserialdev();
    
    // Establish the CPU interface.
    open_cpu_serial(portName, INTERRUPTS);
    
    // Start a thread for each interrupt
    WQ_thread  = thread_start(WQ_handler,  "WQ_handler",  pri_min);
    RQ_thread  = thread_start(RQ_handler,  "RQ_handler",  pri_min);
    EVR_thread = thread_start(EVR_handler, "EVR_handler", pri_min);
    EVQ_thread = thread_start(EVQ_handler, "EVQ_handler", pri_min);


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
    
    // Close the interrupt threads.
    pthread_cancel(WQ_thread);
    pthread_cancel(RQ_thread);
    pthread_cancel(EVR_thread);
    pthread_cancel(EVQ_thread);
    
    // Close the CPU interface.
    close_cpu_serial();

    exit(0);
}

void*
EVQ_handler () {
    for(;;) {
        fpga_EVQ_wait();
        printf("interrupt EVQ\n");
    }
}

void*
EVR_handler () {
    for (;;) {
        fpga_EVR_wait();
        printf("interrupt EVR\n");
    }
}

void*
RQ_handler () {
    for(;;) {
        fpga_RQ_wait();
        printf("interrupt RQ\n");
    }
}

void*
WQ_handler () {
    for (;;) {
        fpga_WQ_wait();
        printf("interrupt WQ\n");
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
            child = fts_children(file_system, 0);

            if (errno != 0)
                perror("error searching /dev for serial interface device");
            while ((NULL != child) && (NULL != child->fts_link)) {
                child = child->fts_link;
                if (strncmp(child->fts_name, usbsearch, usbsearchlen) == 0) {
                    strcpy(portName, dev);
                    strcat(portName, child->fts_name);
                    printf("found port = %s\n", portName);
                }
            }
        }
        if (strlen(portName) == 0) {
            printf("unable to find serial port\n");
            exit(1);
        }
        fts_close(file_system);
    }
}

int compare(const FTSENT** one, const FTSENT** two) {
    return (strcmp((*one)->fts_name, (*two)->fts_name));
}
