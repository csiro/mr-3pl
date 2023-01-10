
// interface "SW" in space "reg" (bus "axi_mgp0")
typedef uint8_t fpga_SW_t;
const unsigned fpga_SW_addr = 4;
static __inline__ void
fpga_SW_write(fpga_SW_t val) {
    FPGA_REG_WRITE(fpga_SW_addr, val, 1);
}

// interface "SR" in space "reg" (bus "axi_mgp0")
typedef uint8_t fpga_SR_t;
const unsigned fpga_SR_addr = 2;
static __inline__ fpga_SR_t
fpga_SR_read() {
    FPGA_REG_READ(fpga_SR_addr, fpga_SR_t, 1);
}

// space and event bitfield for fpga_map() 2nd argument
#define fpga_map_needed \
    fpga_map_none \
    | fpga_space_reg
