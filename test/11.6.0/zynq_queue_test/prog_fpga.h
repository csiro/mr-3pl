
// event "PW"
static __inline__ void
fpga_PW_enable() {
    fpga_event_control(1, 1);
}
static __inline__ void
fpga_PW_disable() {
    fpga_event_control(1, 0);
}
static __inline__ void
fpga_PW_wait() {
    fpga_event_wait(1);
}
static __inline__ int
fpga_PW_count() {
    return(uio_event_count(1));
}

// event "PR"
static __inline__ void
fpga_PR_enable() {
    fpga_event_control(0, 1);
}
static __inline__ void
fpga_PR_disable() {
    fpga_event_control(0, 0);
}
static __inline__ void
fpga_PR_wait() {
    fpga_event_wait(0);
}
static __inline__ int
fpga_PR_count() {
    return(uio_event_count(0));
}

// interface "PE" in space "reg" (bus "axi_mgp0")
typedef uint32_t fpga_PE_t;
const unsigned fpga_PE_addr = 5;
static __inline__ fpga_PE_t
fpga_PE_read() {
    FPGA_REG_READ(fpga_PE_addr, fpga_PE_t, 1);
}

// interface "PW" in space "reg" (bus "axi_mgp0")
typedef uint32_t fpga_PW_t;
const unsigned fpga_PW_addr = 6;
static __inline__ void
fpga_PW_write(fpga_PW_t val) {
    FPGA_REG_WRITE(fpga_PW_addr, val, 1);
}

const unsigned fpga_PW_avail_addr = 4;
static __inline__ uint32_t
fpga_PW_avail_read() {
    FPGA_REG_READ(fpga_PW_avail_addr, uint32_t, 1);
}

const unsigned fpga_PW_avail_thresh_addr = 8;
static __inline__ void
fpga_PW_avail_thresh_write(uint32_t val) {
    FPGA_REG_WRITE(fpga_PW_avail_thresh_addr, val, 1);
}

// interface "PR" in space "reg" (bus "axi_mgp0")
typedef uint32_t fpga_PR_t;
const unsigned fpga_PR_addr = 2;
static __inline__ fpga_PR_t
fpga_PR_read() {
    FPGA_REG_READ(fpga_PR_addr, fpga_PR_t, 1);
}

const unsigned fpga_PR_avail_addr = 3;
static __inline__ uint32_t
fpga_PR_avail_read() {
    FPGA_REG_READ(fpga_PR_avail_addr, uint32_t, 1);
}

const unsigned fpga_PR_avail_thresh_addr = 4;
static __inline__ void
fpga_PR_avail_thresh_write(uint32_t val) {
    FPGA_REG_WRITE(fpga_PR_avail_thresh_addr, val, 1);
}

// interface "PC" in space "reg" (bus "axi_mgp0")
typedef uint16_t fpga_PC_t;
const unsigned fpga_PC_addr = 6;
static __inline__ fpga_PC_t
fpga_PC_read() {
    FPGA_REG_READ(fpga_PC_addr, fpga_PC_t, 1);
}

// interface "WFRED" in space "reg" (bus "axi_mgp0")
typedef uint16_t fpga_WFRED_t;
const unsigned fpga_WFRED_addr = 10;
static __inline__ void
fpga_WFRED_write(fpga_WFRED_t val) {
    FPGA_REG_WRITE(fpga_WFRED_addr, val, 1);
}

// interface "RFRED" in space "reg" (bus "axi_mgp0")
typedef uint16_t fpga_RFRED_t;
const unsigned fpga_RFRED_addr = 7;
static __inline__ fpga_RFRED_t
fpga_RFRED_read() {
    FPGA_REG_READ(fpga_RFRED_addr, fpga_RFRED_t, 1);
}

// space and event bitfield for fpga_map() 2nd argument
#define fpga_map_needed \
    fpga_map_none \
    | fpga_space_reg \
    | fpga_PW_event \
    | fpga_PR_event
