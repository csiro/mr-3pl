#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <pthread.h>
#include "/root/install/3pl/fpga.h"

int64_t     p;

void*       INT_handler ();
pthread_t   thread_start(
                void *  (*func)(void*),
                void *  arg,
                char *  name,
                int     priority
            );

#include    "prog_cinclude.h"

#include    "prog_fpga.h"

int
main (
    int     argc,
    char**  argv
) {    
    pthread_t   INT_thread;
    int         pri_min = 1;

    // Open the CPU interface.
    fpga_open();

    // Load the configuration.
    //fpga_configdata_load(prog_fpga_config_data, prog_fpga_config_size);
    fpga_configfile_load("prog.bit");

    // Start a thread for the interrupt
    INT_thread  = thread_start(INT_handler,  NULL, "INT_handler",  pri_min);

    fpga_A_write(123);
    fpga_B_write(1);
    p = fpga_P_read();
    printf("read %lld (should be 123)\n", p);
    fpga_A_write(1);   // will generate an interrupt
    sleep(2);          // wait for the interrupt
    fpga_B_write(2);
    fpga_B_write(1);   // will generate another interrupt

    // Terminate the interrupt thread.
    pthread_cancel(INT_thread);


    // Close the CPU interface.
    fpga_close();

    exit(0);
}

void*
INT_handler () {
    for(;;) {
        fpga_INT_enable();
        fpga_INT_wait();
        printf("interrupt INT, p = %lld\n", p);
        printf("number of events %d\n", fpga_INT_count());
    }
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
