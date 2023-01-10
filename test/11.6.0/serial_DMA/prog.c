#include <stdio.h>
#include "cpu_serial.h"
#include <fts.h>

static char     usbsearch[] = "tty.usbserial";   // for AVNET board on OSX
static int      usbsearchlen = 12;
static char     dev[6] = "/dev/";
char            portName[200];
uint32_t        buffer[4096];

int         compare (const FTSENT**, const FTSENT**);
void        findserialdev ();

#include    "prog_fpga.h"

int
main (
    int     argc,
    char**  argv
) {
    int i;
    
    for (i=0 ; i<4096 ; i++)
        buffer[i] = i;

    // Find the interface serial device.
    findserialdev();

    // Establish the CPU interface.
    fpga_open(portName);
    
    set_dma_address(&buffer);
        
    printf("\nwrite address 0 to WA\n");
    fpga_WA_write(0);
    fpga_WD_write(0x552244aa);
    sleep(1);          // wait for DMA
    printf("buffer[0] = %x (should be 0x552244aa)\n", buffer[0]);   
        
    printf("\nwrite address 4*3 to WA\n");
    fpga_WA_write(4*3);
    fpga_WD_write(0x31425364);
    sleep(1);          // wait for DMA
    printf("buffer[3] = %x (should be 0x31425364)\n", buffer[3]);   

    printf("\nassign buffer[0] = 0x12345678\n");
    buffer[0] = 0x12345678;
    printf("assign buffer[3] = 0x11335577\n");
    buffer[3] = 0x11335577;

    printf("\nwrite address 0 to RA\n");
    fpga_RA_write(0);
    printf("wait for DMA\n");
    sleep(2);          // wait for DMA
    printf("read RD\n");
    printf("DMA read %x (should be 0x12345678)\n", fpga_RD_read());   

    printf("\nwrite address 12 to RA\n");
    fpga_RA_write(12);
    printf("wait for DMA\n");
    sleep(2);          // wait for DMA
    printf("read RD\n");
    printf("DMA read %x (should be 0x11335577)\n", fpga_RD_read());


    // Close the CPU interface.
    fpga_close();

    exit(0);
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
