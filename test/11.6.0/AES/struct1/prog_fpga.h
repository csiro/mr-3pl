
// interface "SW" in space "reg" 
typedef union {
    struct {
        uint64_t f1: 9;
        int64_t f2: 12;
        uint64_t f3: 1;
        uint64_t f4: 16;
        uint64_t : 26;
    };
    uint64_t __word;
} fpga_SW_t;
const unsigned fpga_SW_addr = 2;
static void
fpga_SW_write(fpga_SW_t val) {
    comms_write(0, fpga_SW_addr, 0, 5, (char *)&val);
}

// interface "SR" in space "reg" 
typedef union {
    struct {
        uint64_t f1: 9;
        int64_t f2: 12;
        uint64_t f3: 1;
        uint64_t f4: 16;
        uint64_t : 26;
    };
    uint64_t __word;
} fpga_SR_t;
const unsigned fpga_SR_addr = 2;
static fpga_SR_t
fpga_SR_read() {
    fpga_SR_t v;
    comms_read(0, fpga_SR_addr, 0, 5, 0, 3, (char *)&v);
    return(v);
}

const unsigned fpga_interrupts = 0;
