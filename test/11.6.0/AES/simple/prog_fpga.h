
// interface "RQ" in space "reg" 
typedef uint16_t fpga_RQ_t;
const unsigned fpga_RQ_addr = 3;
static fpga_RQ_t
fpga_RQ_read() {
    fpga_RQ_t v;
    comms_read(0, fpga_RQ_addr, 0, 2, 0, 0, (char *)&v);
    return(v);
}

// interface "S2R" in space "reg" 
typedef uint32_t fpga_S2R_t;
const unsigned fpga_S2R_addr = 2;
static fpga_S2R_t
fpga_S2R_read() {
    fpga_S2R_t v;
    comms_read(0, fpga_S2R_addr, 0, 4, 0, 0, (char *)&v);
    return(v);
}

// interface "QW" in space "reg" 
typedef uint16_t fpga_QW_t;
const unsigned fpga_QW_addr = 4;
static void
fpga_QW_write(fpga_QW_t val) {
    comms_write(0, fpga_QW_addr, 0, 2, (char *)&val);
}

// interface "S1W" in space "reg" 
typedef uint16_t fpga_S1W_t;
const unsigned fpga_S1W_addr = 2;
static void
fpga_S1W_write(fpga_S1W_t val) {
    comms_write(0, fpga_S1W_addr, 0, 2, (char *)&val);
}

// interface "LW" in space "reg" 
typedef uint8_t fpga_LW_t;
const unsigned fpga_LW_addr = 3;
static void
fpga_LW_write(fpga_LW_t val) {
    comms_write(0, fpga_LW_addr, 0, 1, (char *)&val);
}

const unsigned fpga_interrupts = 0;
