#include    <stdio.h>
#include    <stdlib.h>
#include    <sys/file.h>
#include    <sys/types.h>
#include    <sys/stat.h>
#include    <unistd.h>
#include    <fcntl.h>
#include    <sys/uio.h>
#include    <termios.h>
#include    <signal.h>
#include    <errno.h>
#include    <string.h>
#include    <sched.h>
#include    <pthread.h>
#include    <semaphore.h>
#include    <sys/wait.h>

extern sem_t**          sem;
extern const unsigned   fpga_interrupts;

void        fpga_open(char* pname);
void        fpga_close();
void        set_dma_address (char* addr);
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
pthread_t   thread_start(
                void *  (*func)(void*),
                char *  name,
                int     priority
            );
