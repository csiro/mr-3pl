
// interface "RD" in space "reg" 
typedef uint32_t fpga_RD_t;
const unsigned fpga_RD_addr = 2;
static fpga_RD_t
fpga_RD_read() {
    fpga_RD_t v;
    comms_read(0, fpga_RD_addr, 0, 4, 0, 0, (char *)&v);
    return(v);
}

// interface "RA" in space "reg" 
typedef uint16_t fpga_RA_t;
const unsigned fpga_RA_addr = 4;
static void
fpga_RA_write(fpga_RA_t val) {
    comms_write(0, fpga_RA_addr, 0, 2, (char *)&val);
}

// interface "WD" in space "reg" 
typedef uint32_t fpga_WD_t;
const unsigned fpga_WD_addr = 3;
static void
fpga_WD_write(fpga_WD_t val) {
    comms_write(0, fpga_WD_addr, 0, 4, (char *)&val);
}

// interface "WA" in space "reg" 
typedef uint16_t fpga_WA_t;
const unsigned fpga_WA_addr = 2;
static void
fpga_WA_write(fpga_WA_t val) {
    comms_write(0, fpga_WA_addr, 0, 2, (char *)&val);
}

const unsigned fpga_interrupts = 0;
