
// interface "STARTW" in space "reg" 
typedef uint8_t fpga_STARTW_t;
const unsigned fpga_STARTW_addr = 2;
static void
fpga_STARTW_write(fpga_STARTW_t val) {
    comms_write(0, fpga_STARTW_addr, 0, 1, (char *)&val);
}

// event "EV2"
void
fpga_EV2_wait() {
    sem_wait(sem[1]);
}

// event "EV1"
void
fpga_EV1_wait() {
    sem_wait(sem[0]);
}

const unsigned fpga_interrupts = 2;
