
// interface "SW" in space "reg" 
typedef uint8_t fpga_SW_t;
const unsigned fpga_SW_addr = 4;
static void
fpga_SW_write(fpga_SW_t val) {
    comms_write(0, fpga_SW_addr, 0, 1, (char *)&val);
}

// interface "SR" in space "reg" 
typedef uint8_t fpga_SR_t;
const unsigned fpga_SR_addr = 2;
static fpga_SR_t
fpga_SR_read() {
    fpga_SR_t v;
    comms_read(0, fpga_SR_addr, 0, 1, 0, 0, (char *)&v);
    return(v);
}

#define INTERRUPTS 0
