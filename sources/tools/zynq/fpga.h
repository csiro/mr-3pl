#include <stdio.h>
#include <string.h>
#include <inttypes.h>
#include <stdlib.h>
#include <limits.h>
#include <dirent.h>
#include <sys/mman.h>
#include <sys/time.h>
#include <poll.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>



#define REG_UIO_NAME "axi_mgp0"
#define MEM_UIO_NAME "axi_mgp1"
#define DMA_UIO_NAME "axi_hpbuf"

#define FPGA_NEVENTS 16


// information relating to a space mapping
typedef struct {
    int                 fd;
    char                dev[64];
    char                name[64];
    volatile uint32_t   *base;
    uintptr_t           phys;
    size_t              size;
} fpga_space_info_t;

// information relating to an event
typedef struct {
    int     fd;         // Interrupt file descriptor
    char    dev[64];    // Interrupt file device
    char    name[64];   // Interrupt name string
    int     unit;       // x for uiox
    int     enumber;    // last event number on servicing interrupt
    int     events;     // number of events since last interrupt servicing
} fpga_event_info_t;

fpga_space_info_t   reg_space;
fpga_space_info_t   mem_space;
fpga_space_info_t   dma_space;

fpga_event_info_t events[FPGA_NEVENTS];

void                fpga_open(void);                                      
void                fpga_close(void);                                     
void                fpga_event_wait(uint32_t event_index);                
void                fpga_event_control(uint32_t event_index, uint32_t c); 
void                fpga_configfile_load(char *filename );         
void                fpga_configdata_load(uint8_t *data, size_t len);      
uintptr_t           uio_dma_phys_address(void);                           
uint32_t            uio_dma_get_word(uint32_t index);                     
void                uio_dma_put_word(uint32_t index, uint32_t w);         
fpga_space_info_t   uio_map_space(char *name);                      
void                uio_unmap_space(fpga_space_info_t space);             
void                uio_map_event(int index);                             
void                uio_unmap_event(int index);                           
int                 uio_find(char *name);                           
int                 uio_event_fd(uint32_t event_index);
int                 uio_event_count(uint32_t event_index);
uintptr_t           uio_dma_phys_address(void);


// Support for info2c-generated C header files
//
// These FPGA_* macros are emitted by info2c inside generated inline functions.
// If desired, these macros can be overridden in the user program, after
// including "libfpga.h" but before including the info2c-generated file,
// usually <design>.h.
//
// THESE MACROS ARE NOT INTENDED FOR USER CODE -- use the C functions below!
//
// Arguments:
// 	event		event to wait for
// 	control		enable/disable (foga_event_control_t)

#define FPGA_EVENT_WAIT(event) \
    fpga_event_wait(event)

#define FPGA_EVENT_CONTROL(event, control) \
    fpga_event_control(event, control)


// For register and memory interfaces, info2c emits one or more inline
// functions, which contain these FPGA_* macros in these contexts:
//
// static __inline__ fpga_NAME_t
// fpga_NAME_read(fpga_t *fpga, unsigned loc) {
//     fpga_NAME_t local;
//     FPGA_MEM_READ(fpga, fpga_NAME_addr, loc, local, 1)
//     return local;
// }
// 
// static __inline__ void
// fpga_NAME_write(fpga_t *fpga, unsigned loc, fpga_NAME_t val) {
//     FPGA_MEM_WRITE(fpga, fpga_NAME_addr, loc, val, 1)
// }
//	addr		register number
//	type		C type of register/user data
//	val		value to be written
//	dww		number of 32-bit words in type

#define FPGA_REG_READ(addr, type, dww) \
    do { \
	type local; \
        fpga_burst_read( \
	    reg_space.base + addr, (uint32_t *)&local, dww \
	); \
	return local; \
    } while (0)

#define FPGA_REG_WRITE(addr, val, dww) \
    fpga_burst_write(reg_space.base + addr, (uint32_t *)&val, dww )


// Memory read and writes take an additional loc argument, but are otherwise
// the same as the register ones.

#define FPGA_MEM_READ(addr, loc, type, dww) \
    do { type local; \
        fpga_burst_read( \
	    mem_space.base + addr + loc, (uint32_t *)&local, dww\
	); \
	return local; \
    } while (0)

#define FPGA_MEM_WRITE(addr, loc, val, dww) \
    fpga_burst_write( \
	mem_space.base + addr + loc, \
	(uint32_t *)&val, dww \
    )



// Burst-mode read/write functions
//
// 3pl register/memory accesses require "burst" mode read and write bus cycles
// for interfaces wider then bus data width (32 bits). The following inline
// functions emit ARM assembly code that does that.
//
// For other architectures, there are fallbacks written in C, but they only
// work for accesses to 32-bit registers. On ARM these fallbacks can be
// forced by defining the symbol FPGA_BURST_FALLBACK.

#define list_r2_to_r3()	"r2", "r3"
#define list_r2_to_r4()	list_r2_to_r3(), "r4"
#define list_r2_to_r5()	list_r2_to_r4(), "r5"
#define list_r2_to_r6()	list_r2_to_r5(), "r6"
#define list_r2_to_r7()	list_r2_to_r6(), "r7"
#define list_r2_to_r8()	list_r2_to_r7(), "r8"
#define list_r2_to_r9()	list_r2_to_r8(), "r9"

#define case_burst_n(nw, lr, la, sa, da, ma, ty) \
    case nw: \
	__asm__ __volatile__ ( \
	    "@ burst "#ty" "#nw" words using ldm/stm on r2-r"#lr"\n\t" \
	    "ldm\t%"#la", {r2-r"#lr"}\n\tstm\t%"#sa", {r2-r"#lr"}" \
	    : : "r" (ma), "r" (da) : "memory", list_r2_to_r##lr() \
	); \
	break

static __inline__ void
fpga_burst_read(volatile uint32_t *devaddr, uint32_t *memaddr, int dww) {
    switch (dww) {
	case 1: *memaddr = *devaddr; break;
	case_burst_n(2, 3, 1, 0, devaddr, memaddr, read);
	case_burst_n(3, 4, 1, 0, devaddr, memaddr, read);
	case_burst_n(4, 5, 1, 0, devaddr, memaddr, read);
	case_burst_n(5, 6, 1, 0, devaddr, memaddr, read);
	case_burst_n(6, 7, 1, 0, devaddr, memaddr, read);
	case_burst_n(7, 8, 1, 0, devaddr, memaddr, read);
	case_burst_n(8, 9, 1, 0, devaddr, memaddr, read);
	default: abort(); break;
    }
}

static __inline__ void
fpga_burst_write(volatile uint32_t *devaddr, uint32_t *memaddr, int dww) {
    switch (dww) {
	case 1: *devaddr = *memaddr; break;
	case_burst_n(2, 3, 0, 1, devaddr, memaddr, write);
	case_burst_n(3, 4, 0, 1, devaddr, memaddr, write);
	case_burst_n(4, 5, 0, 1, devaddr, memaddr, write);
	case_burst_n(5, 6, 0, 1, devaddr, memaddr, write);
	case_burst_n(6, 7, 0, 1, devaddr, memaddr, write);
	case_burst_n(7, 8, 0, 1, devaddr, memaddr, write);
	case_burst_n(8, 9, 0, 1, devaddr, memaddr, write);
	default: abort(); break;
    }
}
