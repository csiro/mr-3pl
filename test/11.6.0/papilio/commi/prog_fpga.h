
// interface "WWW" in space "reg" 
typedef uint8_t fpga_WWW_t;
const unsigned fpga_WWW_addr = 4;
static void
fpga_WWW_write(fpga_WWW_t val) {
    comms_write(0, fpga_WWW_addr, 0, 1, (char *)&val);
}

// interface "R" in space "reg" 
typedef uint16_t fpga_R_t;
const unsigned fpga_R_addr = 2;
static fpga_R_t
fpga_R_read() {
    fpga_R_t v;
    comms_read(0, fpga_R_addr, 0, 2, 0, 0, (char *)&v);
    return(v);
}

// interface "RR" in space "reg" 
typedef int64_t fpga_RR_t;
const unsigned fpga_RR_addr = 3;
static fpga_RR_t
fpga_RR_read() {
    fpga_RR_t v;
    comms_read(0, fpga_RR_addr, 0, 5, 1, 3, (char *)&v);
    if ((v & 0x400000000) != 0)
        v |= 0xfffffff800000000;
    return(v);
}

// interface "RQ" in space "reg" 
typedef int16_t fpga_RQ_t;
const unsigned fpga_RQ_addr = 6;
static fpga_RQ_t
fpga_RQ_read() {
    fpga_RQ_t v;
    comms_read(0, fpga_RQ_addr, 0, 2, 1, 0, (char *)&v);
    if ((v & 0x2000) != 0)
        v |= 0xc000;
    return(v);
}

// interface "RQ_avail" in space "reg" 
typedef uint8_t fpga_RQ_avail_t;
const unsigned fpga_RQ_avail_addr = 7;
static fpga_RQ_avail_t
fpga_RQ_avail_read() {
    fpga_RQ_avail_t v;
    comms_read(0, fpga_RQ_avail_addr, 0, 1, 0, 0, (char *)&v);
    return(v);
}

// interface "RQ_avail_thresh" in space "reg" 
typedef uint8_t fpga_RQ_avail_thresh_t;
const unsigned fpga_RQ_avail_thresh_addr = 7;
static void
fpga_RQ_avail_thresh_write(fpga_RQ_avail_thresh_t val) {
    comms_write(0, fpga_RQ_avail_thresh_addr, 0, 1, (char *)&val);
}

// interface "W" in space "reg" 
typedef uint16_t fpga_W_t;
const unsigned fpga_W_addr = 2;
static void
fpga_W_write(fpga_W_t val) {
    comms_write(0, fpga_W_addr, 0, 2, (char *)&val);
}

// interface "RRR" in space "reg" 
typedef uint8_t fpga_RRR_t;
const unsigned fpga_RRR_addr = 4;
static fpga_RRR_t
fpga_RRR_read() {
    fpga_RRR_t v;
    comms_read(0, fpga_RRR_addr, 0, 1, 0, 0, (char *)&v);
    return(v);
}

// interface "MW" in space "reg" 
typedef uint16_t fpga_MW_t;
const unsigned fpga_MW_addr = 8;
static void
fpga_MW_write(fpga_MW_t val, uint16_t mem_addr) {
    comms_write(1, fpga_MW_addr, mem_addr, 2, (char *)&val);
}

// interface "WW" in space "reg" 
typedef int64_t fpga_WW_t;
const unsigned fpga_WW_addr = 3;
static void
fpga_WW_write(fpga_WW_t val) {
    comms_write(0, fpga_WW_addr, 0, 5, (char *)&val);
}

// interface "MR" in space "reg" 
typedef uint16_t fpga_MR_t;
const unsigned fpga_MR_addr = 9;
static fpga_MR_t
fpga_MR_read(uint16_t mem_addr) {
    fpga_MR_t v;
    comms_read(1, fpga_MR_addr, mem_addr, 2, 0, 0, (char *)&v);
    return(v);
}

// interface "WQ" in space "reg" 
typedef int16_t fpga_WQ_t;
const unsigned fpga_WQ_addr = 5;
static void
fpga_WQ_write(fpga_WQ_t val) {
    comms_write(0, fpga_WQ_addr, 0, 2, (char *)&val);
}

// interface "WQ_avail" in space "reg" 
typedef uint8_t fpga_WQ_avail_t;
const unsigned fpga_WQ_avail_addr = 5;
static fpga_WQ_avail_t
fpga_WQ_avail_read() {
    fpga_WQ_avail_t v;
    comms_read(0, fpga_WQ_avail_addr, 0, 1, 0, 0, (char *)&v);
    return(v);
}

// interface "WQ_avail_thresh" in space "reg" 
typedef uint8_t fpga_WQ_avail_thresh_t;
const unsigned fpga_WQ_avail_thresh_addr = 6;
static void
fpga_WQ_avail_thresh_write(fpga_WQ_avail_thresh_t val) {
    comms_write(0, fpga_WQ_avail_thresh_addr, 0, 1, (char *)&val);
}

// interface "RQO" in space "reg" 
typedef int16_t fpga_RQO_t;
const unsigned fpga_RQO_addr = 8;
static fpga_RQO_t
fpga_RQO_read() {
    fpga_RQO_t v;
    comms_read(0, fpga_RQO_addr, 0, 2, 1, 0, (char *)&v);
    if ((v & 0x2000) != 0)
        v |= 0xc000;
    return(v);
}

// event "EVR"
void
fpga_EVR_wait() {
    sem_wait(sem[0]);
}

// event "EVQ"
void
fpga_EVQ_wait() {
    sem_wait(sem[3]);
}

// event "RQ"
void
fpga_RQ_wait() {
    sem_wait(sem[2]);
}

// event "WQ"
void
fpga_WQ_wait() {
    sem_wait(sem[1]);
}

const unsigned fpga_interrupts = 4;
