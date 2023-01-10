#include <stdio.h>
#include <stdint.h>
#include "fpga.h"
#include "prog.h"




int main (int argc, const char *argv[]) {
    int         i;
    
    fpga_open();
    fpga_configfile_load("prog.bit");

    printf("CHECK %d (1234)\n", fpga_RFRED_read());
    fpga_WFRED_write(4321);
    printf("CHECK %d (4321)\n", fpga_RFRED_read());

    printf("initial PR avail %d (0)\n", fpga_PR_avail_read());
    printf("initial PW avail %d (512)\n", fpga_PW_avail_read());
    printf("initial queue count %d\n", fpga_PC_read());
    printf("initial queue examine %d\n", fpga_PE_read());
    
    fpga_PW_write(7);
    fpga_PW_write(15);
    printf("PR avail after 2 writes %d (2)\n", fpga_PR_avail_read());
    printf("PW avail after 2 writes %d (510)\n", fpga_PW_avail_read());
    printf("queue count after 2 writes %d\n", fpga_PC_read());
    printf("queue examine after 2 writes %d\n", fpga_PE_read());
    
    
    fpga_PR_avail_thresh_write(4);
    fpga_PW_avail_thresh_write(20);
    fpga_PW_enable();
    fpga_PW_wait(0.0, NULL);
    printf("have write threshold interrupt\n");
    fpga_PW_write(31);
    fpga_PW_write(33);
    fpga_PW_disable();

    printf("PR avail after 4 writes %d (4)\n", fpga_PR_avail_read());
    printf("PW avail after 4 writes %d (508)\n", fpga_PW_avail_read());

    fpga_PR_enable();
    fpga_PR_wait(0.0, NULL);
    printf("have read threshold interrupt\n");
    while (fpga_PR_avail_read() > 0)
        printf("read %d\n", fpga_PR_read());
    fpga_PR_disable();

    printf("PR avail after all reads %d (0)\n", fpga_PR_avail_read());
    printf("PW avail after all reads %d (512)\n", fpga_PW_avail_read());

    fpga_close();
    
    exit(0);
}
