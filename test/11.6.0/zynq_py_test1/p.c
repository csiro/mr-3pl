#include    <stdlib.h>
#include    <stdio.h>

#define uint32_t unsigned int

#define list_r2_to_r3() "r2", "r3"
#define list_r2_to_r4() list_r2_to_r3(), "r4"
#define list_r2_to_r5() list_r2_to_r4(), "r5"
#define list_r2_to_r6() list_r2_to_r5(), "r6"
#define list_r2_to_r7() list_r2_to_r6(), "r7"
#define list_r2_to_r8() list_r2_to_r7(), "r8"
#define list_r2_to_r9() list_r2_to_r8(), "r9"

#define case_burst_n(nw, lr, la, sa, da, ma, ty) \
    case nw: \
        __asm__ __volatile__ ( \
            "@ burst "#ty" "#nw" words using ldm/stm on r2-r"#lr"\n\t" \
            "ldm\t%"#la", {r2-r"#lr"}\n\tstm\t%"#sa", {r2-r"#lr"}" \
            : : "r" (ma), "r" (da) : "memory", list_r2_to_r##lr() \
        ); \
        break

static void fpga_burst_read(volatile uint32_t *devaddr, uint32_t *memaddr, int dww);
static void fpga_burst_write(volatile uint32_t *devaddr, uint32_t *memaddr, int dww);

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

int main () {
    unsigned int s, d;
    
    fpga_burst_read(&s, &d, 2);
    fpga_burst_write(&d, &s, 2);
}
