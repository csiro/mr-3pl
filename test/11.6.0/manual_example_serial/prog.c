#include <stdio.h>
#include "cpu_serial.h"
#include <fts.h>

static char     usbsearch[] = "tty.usbmodem";   // for AVNET board on OSX
static int      usbsearchlen = 12;
static char     dev[6] = "/dev/";
char            portName[200];

int         compare (const FTSENT**, const FTSENT**);
void        findserialdev ();
void*       INT_handler ();

#include    "prog_fpga.h"

int
main (
    int     argc,
    char**  argv
) {    
    pthread_t   INT_thread;
    int         pri_min = 1;

    // Find the interface serial device.
    findserialdev();

    // Establish the CPU interface.
    fpga_open(portName);

    // Start a thread for the interrupt
    INT_thread  = thread_start(INT_handler,  "INT_handler",  pri_min);

    fpga_A_write(123);
    fpga_B_write(1);
    printf("read %lld (should be 123)\n", fpga_P_read());
    fpga_A_write(1);   // will generate an interrupt
    printf("wait for interrupt\n");
    sleep(1);          // wait for the interrupt

    printf("close and exit\n");
    // Close the interrupt thread.
    pthread_cancel(INT_thread);


    // Close the CPU interface.
    fpga_close();

    exit(0);
}

void*
INT_handler () {
    for(;;) {
        fpga_INT_wait();
        printf("interrupt INT\n");
    }
}

// Search for a serial port.
// 'usbsearch' gives the head of the device name and 'usbsearchlen' is the
// length of string to search.
// IF MORE THAN ONE PORT MATCHES, THE LAST IS USED.
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
