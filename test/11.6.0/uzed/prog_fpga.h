
// event "EVQ"
static __inline__ void
fpga_EVQ_enable() {
    fpga_event_control(3, 1);
}
static __inline__ void
fpga_EVQ_disable() {
    fpga_event_control(3, 0);
}
static __inline__ void
fpga_EVQ_wait() {
    fpga_event_wait(3);
}
static __inline__ int
fpga_EVQ_count() {
    return(uio_event_count(3));
}

// event "WQ"
static __inline__ void
fpga_WQ_enable() {
    fpga_event_control(1, 1);
}
static __inline__ void
fpga_WQ_disable() {
    fpga_event_control(1, 0);
}
static __inline__ void
fpga_WQ_wait() {
    fpga_event_wait(1);
}
static __inline__ int
fpga_WQ_count() {
    return(uio_event_count(1));
}

// event "EVR"
static __inline__ void
fpga_EVR_enable() {
    fpga_event_control(0, 1);
}
static __inline__ void
fpga_EVR_disable() {
    fpga_event_control(0, 0);
}
static __inline__ void
fpga_EVR_wait() {
    fpga_event_wait(0);
}
static __inline__ int
fpga_EVR_count() {
    return(uio_event_count(0));
}

// event "RQ"
static __inline__ void
fpga_RQ_enable() {
    fpga_event_control(2, 1);
}
static __inline__ void
fpga_RQ_disable() {
    fpga_event_control(2, 0);
}
static __inline__ void
fpga_RQ_wait() {
    fpga_event_wait(2);
}
static __inline__ int
fpga_RQ_count() {
    return(uio_event_count(2));
}

// interface "WQ" in space "reg" (bus "axi_mgp0")
typedef int16_t fpga_WQ_t;
const unsigned fpga_WQ_addr = 10;
static __inline__ void
fpga_WQ_write(fpga_WQ_t val) {
    FPGA_REG_WRITE(fpga_WQ_addr, val, 1);
}

const unsigned fpga_WQ_avail_addr = 5;
static __inline__ uint32_t
fpga_WQ_avail_read() {
    FPGA_REG_READ(fpga_WQ_avail_addr, uint32_t, 1);
}

const unsigned fpga_WQ_avail_thresh_addr = 12;
static __inline__ void
fpga_WQ_avail_thresh_write(uint32_t val) {
    FPGA_REG_WRITE(fpga_WQ_avail_thresh_addr, val, 1);
}

// interface "WWW" in space "reg" (bus "axi_mgp0")
typedef uint8_t fpga_WWW_t;
const unsigned fpga_WWW_addr = 8;
static __inline__ void
fpga_WWW_write(fpga_WWW_t val) {
    FPGA_REG_WRITE(fpga_WWW_addr, val, 1);
}

// interface "RQ" in space "reg" (bus "axi_mgp0")
typedef int16_t fpga_RQ_t;
const unsigned fpga_RQ_addr = 6;
static __inline__ fpga_RQ_t
fpga_RQ_read() {
    FPGA_REG_READ(fpga_RQ_addr, fpga_RQ_t, 1);
}

const unsigned fpga_RQ_avail_addr = 7;
static __inline__ uint32_t
fpga_RQ_avail_read() {
    FPGA_REG_READ(fpga_RQ_avail_addr, uint32_t, 1);
}

const unsigned fpga_RQ_avail_thresh_addr = 14;
static __inline__ void
fpga_RQ_avail_thresh_write(uint32_t val) {
    FPGA_REG_WRITE(fpga_RQ_avail_thresh_addr, val, 1);
}

// interface "RRR" in space "reg" (bus "axi_mgp0")
typedef uint8_t fpga_RRR_t;
const unsigned fpga_RRR_addr = 4;
static __inline__ fpga_RRR_t
fpga_RRR_read() {
    FPGA_REG_READ(fpga_RRR_addr, fpga_RRR_t, 1);
}

// interface "W" in space "reg" (bus "axi_mgp0")
typedef uint16_t fpga_W_t;
const unsigned fpga_W_addr = 4;
static __inline__ void
fpga_W_write(fpga_W_t val) {
    FPGA_REG_WRITE(fpga_W_addr, val, 1);
}

// interface "RR" in space "reg" (bus "axi_mgp0")
typedef int64_t fpga_RR_t;
const unsigned fpga_RR_addr = 3;
static __inline__ fpga_RR_t
fpga_RR_read() {
    FPGA_REG_READ(fpga_RR_addr, fpga_RR_t, 2);
}

// interface "R" in space "reg" (bus "axi_mgp0")
typedef uint16_t fpga_R_t;
const unsigned fpga_R_addr = 2;
static __inline__ fpga_R_t
fpga_R_read() {
    FPGA_REG_READ(fpga_R_addr, fpga_R_t, 1);
}

// interface "WW" in space "reg" (bus "axi_mgp0")
typedef int64_t fpga_WW_t;
const unsigned fpga_WW_addr = 6;
static __inline__ void
fpga_WW_write(fpga_WW_t val) {
    FPGA_REG_WRITE(fpga_WW_addr, val, 2);
}

// interface "MW" in space "mem" (bus "axi_mgp1")
typedef uint16_t fpga_MW_t;
const unsigned fpga_MW_addr = 512;
static __inline__ void
fpga_MW_write(fpga_t *fpga, unsigned loc, fpga_MW_t val) {
    FPGA_MEM_WRITE(fpga_MW_addr, loc, val, 1);
}

// interface "MR" in space "mem" (bus "axi_mgp1")
typedef uint16_t fpga_MR_t;
const unsigned fpga_MR_addr = 256;
static __inline__ fpga_MR_t
fpga_MR_read(fpga_t *fpga, unsigned loc) {
    FPGA_MEM_READ(fpga_MR_addr, loc, fpga_MR_t, 1);
}

// space and event bitfield for fpga_map() 2nd argument
#define fpga_map_needed \
    fpga_map_none \
    | fpga_space_reg \
    | fpga_space_mem \
    | fpga_EVQ_event \
    | fpga_WQ_event \
    | fpga_EVR_event \
    | fpga_RQ_event
