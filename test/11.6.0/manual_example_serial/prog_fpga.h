
// interface "P" in space "reg" 
typedef int64_t fpga_P_t;
const unsigned fpga_P_addr = 2;
static fpga_P_t
fpga_P_read() {
    fpga_P_t v;
    comms_read(0, fpga_P_addr, 0, 6, 1, 2, (char *)&v);
    if ((v & 0x800000000000) != 0)
        v |= 0xffff000000000000;
    return(v);
}

// interface "B" in space "reg" 
typedef int32_t fpga_B_t;
const unsigned fpga_B_addr = 3;
static void
fpga_B_write(fpga_B_t val) {
    comms_write(0, fpga_B_addr, 0, 3, (char *)&val);
}

// interface "A" in space "reg" 
typedef int32_t fpga_A_t;
const unsigned fpga_A_addr = 2;
static void
fpga_A_write(fpga_A_t val) {
    comms_write(0, fpga_A_addr, 0, 4, (char *)&val);
}

// event "INT"
void
fpga_INT_wait() {
    sem_wait(sem[0]);
}

const unsigned fpga_interrupts = 1;
