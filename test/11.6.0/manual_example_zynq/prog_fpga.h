
// event "INT"
static __inline__ void
fpga_INT_enable() {
    fpga_event_control(0, 1);
}
static __inline__ void
fpga_INT_disable() {
    fpga_event_control(0, 0);
}
static __inline__ void
fpga_INT_wait() {
    fpga_event_wait(0);
}
static __inline__ int
fpga_INT_count() {
    return(uio_event_count(0));
}

// interface "A" in space "reg" (bus "axi_mgp0")
typedef int32_t fpga_A_t;
const unsigned fpga_A_addr = 4;
static __inline__ void
fpga_A_write(fpga_A_t val) {
    FPGA_REG_WRITE(fpga_A_addr, val, 1);
}

// interface "P" in space "reg" (bus "axi_mgp0")
typedef int64_t fpga_P_t;
const unsigned fpga_P_addr = 2;
static __inline__ fpga_P_t
fpga_P_read() {
    FPGA_REG_READ(fpga_P_addr, fpga_P_t, 2);
}

// interface "B" in space "reg" (bus "axi_mgp0")
typedef int32_t fpga_B_t;
const unsigned fpga_B_addr = 6;
static __inline__ void
fpga_B_write(fpga_B_t val) {
    FPGA_REG_WRITE(fpga_B_addr, val, 1);
}

// space and event bitfield for fpga_map() 2nd argument
#define fpga_map_needed \
    fpga_map_none \
    | fpga_space_reg \
    | fpga_INT_event
