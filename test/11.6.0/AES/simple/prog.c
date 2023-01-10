#include    "/Users/dun202/install/3pl/include/cpu_serial.h"
#include    <fts.h>
#include    <math.h>



static char     usbsearch[] = "tty.usbmodem";   // for AVnet board on OSX
static int      usbsearchlen = 12;
static char     dev[6] = "/dev/";
char            portName[200];

int         compare (const FTSENT**, const FTSENT**);
void        findserialdev ();

#include    "prog_fpga.h"

int
main (
    int     argc,
    char**  argv
) {    
    
    // Find the interface serial device.
    findserialdev();
    
    // Establish the CPU interface.
    open_cpu_serial(portName, INTERRUPTS);


    fpga_LW_write(7);

    fpga_S1W_write(47);
    printf("%d\n", fpga_S2R_read());

  
    // Close the CPU interface.
    close_cpu_serial();

    exit(0);
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
