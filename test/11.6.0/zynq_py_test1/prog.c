#include <stdio.h>
#include "prog.h"

fpga_t *fpga = NULL;

int main_fpga(int argc, const char *argv[]);

int main(int argc, const char *argv[]) {
    error_init();
    if (main_fpga(argc, argv)) {
        fprintf(stderr, "%s: %s\n", argv[0], error_message_get());
	if (fpga)
	    fpga_close(fpga);
        return 1;
    }
    return 0;
}

int main_fpga(int argc, const char *argv[]) {

    nrete( fpga = fpga_open(fpga_unit_default, fpga_flag_default) );
    erete( fpga_configfile_load(fpga, "prog", NULL) );
    printf("%d\n", fpga_R_read(fpga));
    erete( fpga_close(fpga) );
    return status_ok;
}
