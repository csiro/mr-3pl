
// event "RD"
static __inline__ void
fpga_RD_wait() {
    FPGA_EVENT_WAIT(2);
}
static __inline__ void
fpga_RD_enable() {
    FPGA_EVENT_CONTROL(2, 1);
}
static __inline__ void
fpga_RD_disable() {
    FPGA_EVENT_CONTROL(2, 0);
}

// event "I"
static __inline__ void
fpga_I_wait() {
    FPGA_EVENT_WAIT(0);
}
static __inline__ void
fpga_I_enable() {
    FPGA_EVENT_CONTROL(0, 1);
}
static __inline__ void
fpga_I_disable() {
    FPGA_EVENT_CONTROL(0, 0);
}

// event "RA"
static __inline__ void
fpga_RA_wait() {
    FPGA_EVENT_WAIT(1);
}
static __inline__ void
fpga_RA_enable() {
    FPGA_EVENT_CONTROL(1, 1);
}
static __inline__ void
fpga_RA_disable() {
    FPGA_EVENT_CONTROL(1, 0);
}

// interface "RD" in space "reg" (bus "axi_mgp0")
typedef uint64_t fpga_RD_t;
const unsigned fpga_RD_addr = 5;
static __inline__ fpga_RD_t
fpga_RD_read() {
    FPGA_REG_READ(fpga_RD_addr, fpga_RD_t, 2);
}

const unsigned fpga_RD_avail_addr = 6;
static __inline__ uint32_t
fpga_RD_avail_read() {
    FPGA_REG_READ(fpga_RD_avail_addr, uint32_t, 1);
}

const unsigned fpga_RD_avail_thresh_addr = 22;
static __inline__ void
fpga_RD_avail_thresh_write(uint32_t val) {
    FPGA_REG_WRITE(fpga_RD_avail_thresh_addr, val, 1);
}

// interface "IW" in space "reg" (bus "axi_mgp0")
typedef bool_t fpga_IW_t;
const unsigned fpga_IW_addr = 8;
static __inline__ void
fpga_IW_write(fpga_IW_t val) {
    FPGA_REG_WRITE(fpga_IW_addr, val, 1);
}

// interface "BIGR" in space "reg" (bus "axi_mgp0")
typedef struct {uint32_t v5;}  fpga_BIGR_t;
const unsigned fpga_BIGR_addr = 7;
static __inline__ fpga_BIGR_t
fpga_BIGR_read() {
    FPGA_REG_READ(fpga_BIGR_addr, fpga_BIGR_t, 5);
}

// interface "WW" in space "reg" (bus "axi_mgp0")
typedef int64_t fpga_WW_t;
const unsigned fpga_WW_addr = 6;
static __inline__ void
fpga_WW_write(fpga_WW_t val) {
    FPGA_REG_WRITE(fpga_WW_addr, val, 2);
}

// interface "RR" in space "reg" (bus "axi_mgp0")
typedef int64_t fpga_RR_t;
const unsigned fpga_RR_addr = 3;
static __inline__ fpga_RR_t
fpga_RR_read() {
    FPGA_REG_READ(fpga_RR_addr, fpga_RR_t, 2);
}

// interface "infoword" in space "reg" (bus "axi_mgp0")
typedef uint32_t fpga_infoword_t;
const unsigned fpga_infoword_addr = 1;
static __inline__ fpga_infoword_t
fpga_infoword_read() {
    FPGA_REG_READ(fpga_infoword_addr, fpga_infoword_t, 1);
}

// interface "WA" in space "reg" (bus "axi_mgp0")
typedef uint32_t fpga_WA_t;
const unsigned fpga_WA_addr = 10;
static __inline__ void
fpga_WA_write(fpga_WA_t val) {
    FPGA_REG_WRITE(fpga_WA_addr, val, 1);
}

// interface "RL" in space "reg" (bus "axi_mgp0")
typedef uint8_t fpga_RL_t;
const unsigned fpga_RL_addr = 20;
static __inline__ void
fpga_RL_write(fpga_RL_t val) {
    FPGA_REG_WRITE(fpga_RL_addr, val, 1);
}

// interface "WD" in space "reg" (bus "axi_mgp0")
typedef uint64_t fpga_WD_t;
const unsigned fpga_WD_addr = 14;
static __inline__ void
fpga_WD_write(fpga_WD_t val) {
    FPGA_REG_WRITE(fpga_WD_addr, val, 2);
}

// interface "R" in space "reg" (bus "axi_mgp0")
typedef int16_t fpga_R_t;
const unsigned fpga_R_addr = 2;
static __inline__ fpga_R_t
fpga_R_read() {
    FPGA_REG_READ(fpga_R_addr, fpga_R_t, 1);
}

// interface "BIGW" in space "reg" (bus "axi_mgp0")
typedef struct {uint32_t v5;}  fpga_BIGW_t;
const unsigned fpga_BIGW_addr = 24;
static __inline__ void
fpga_BIGW_write(fpga_BIGW_t val) {
    FPGA_REG_WRITE(fpga_BIGW_addr, val, 5);
}

// interface "W" in space "reg" (bus "axi_mgp0")
typedef int16_t fpga_W_t;
const unsigned fpga_W_addr = 4;
static __inline__ void
fpga_W_write(fpga_W_t val) {
    FPGA_REG_WRITE(fpga_W_addr, val, 1);
}

// interface "WL" in space "reg" (bus "axi_mgp0")
typedef uint8_t fpga_WL_t;
const unsigned fpga_WL_addr = 12;
static __inline__ void
fpga_WL_write(fpga_WL_t val) {
    FPGA_REG_WRITE(fpga_WL_addr, val, 1);
}

// interface "RA" in space "reg" (bus "axi_mgp0")
typedef uint32_t fpga_RA_t;
const unsigned fpga_RA_addr = 16;
static __inline__ void
fpga_RA_write(fpga_RA_t val) {
    FPGA_REG_WRITE(fpga_RA_addr, val, 1);
}

const unsigned fpga_RA_avail_addr = 4;
static __inline__ uint32_t
fpga_RA_avail_read() {
    FPGA_REG_READ(fpga_RA_avail_addr, uint32_t, 1);
}

const unsigned fpga_RA_avail_thresh_addr = 18;
static __inline__ void
fpga_RA_avail_thresh_write(uint32_t val) {
    FPGA_REG_WRITE(fpga_RA_avail_thresh_addr, val, 1);
}

// interface "inforeset" in space "reg" (bus "axi_mgp0")
const unsigned fpga_inforeset_addr = 2;
static __inline__ void
fpga_inforeset_write() {
    uint32_t    val = 0;
    FPGA_REG_WRITE(fpga_inforeset_addr, val, 1);
}

// space and event bitfield for fpga_map() 2nd argument
#define fpga_map_needed \
    fpga_map_none \
    | fpga_space_reg \
    | fpga_RD_event \
    | fpga_I_event \
    | fpga_RA_event
