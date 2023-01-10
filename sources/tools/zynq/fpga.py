import os
from fpga_read_write import fpga_c_open, fpga_c_close,\
                            fpga_configfile_load, fpga_configdata_load,\
                            fpga_burst_read, fpga_burst_write,\
                            uio_event_wait, uio_event_fd, uio_event_unit, \
                            uio_event_count,\
                            uio_event_control, uio_dma_get_word,\
                            uio_dma_put_word, uio_dma_phys_address
import sys

global  enumber
global  events
enumber = []
events = []
for i in range(15):
    enumber.append(0)
    events.append(0)

def fpga_open ():
    fpga_c_open()
    for i in range(15):
        fname = '/sys/class/uio/uio' + str(uio_event_unit(i)) + '/event'
        with open(fname, 'r', encoding="utf-8") as f:
            v = int(f.read())
            enumber[i] = v

def fpga_close():
    fpga_c_close()
    
# Write data to FPGA.
#   mem         0 if static or queue write, 1 if memory write
#   address     FPGA interface address
#   memaddress  memory address if mem == 1
#   dba         byte array of data to send
#
def comms_write (mem, address, memaddress, dba):
    # If dba is None send a single 32-bit word of value 1 (true).
    # This occurs on a write with no data.
    if dba == None:
        v = 1
        dba = v.to_bytes(4, byteorder='little')
    if mem != 0:
        fpga_burst_write(mem, address + memaddress, dba, (len(dba) + 3) // 4)
    else:
        fpga_burst_write(mem, address, dba, (len(dba) + 3) // 4)
    

# Read data from FPGA.
#   mem         0 if value or queue read, 1 if memory read
#   address     FPGA interface address
#   memaddress  memory address if mem == 1
#   bytes       data length
#
def comms_read (mem, address, memaddress, bytes):
    if mem != 0:
        rba = fpga_burst_read(mem, address + memaddress, (bytes + 3) // 4)       
    else:
        rba = fpga_burst_read(mem, address, (bytes + 3) // 4)
    return rba

# Enable event.
#   ev      event number
#
def event_enable (ev):
    fd = uio_event_fd(ev)
    val = 1
    wba = val.to_bytes(4, byteorder='little', signed=True)
    os.write(fd, wba)

# Disable event.
#   ev      event number
#
def event_disable (ev):
    fd = uio_event_fd(ev)
    val = 0
    wba = val.to_bytes(4, byteorder='little', signed=True)
    os.write(fd, wba)

# Wait for event.
#   ev      event number
#
def event_wait (ev):
    #event_enable(ev)
    #uio_event_wait(ev)

    
    # enable event first
    fd = uio_event_fd(ev)
    val = 1
    wba = val.to_bytes(4, byteorder='little', signed=True)
    os.write(fd, wba)
    
    # wait for event
    ret = os.read(fd, 4)
    
    # Determine number of events
    n = int.from_bytes(ret, byteorder='little', signed=False)
    if n < enumber[ev]:
        events[ev] = enumber[ev] ^ 0xffffffff + 1 + n
    else:
        events[ev] = n - enumber[ev]
    enumber[ev] = n
    # event now disabled

# Get event count.
#   ev      event number
#
def event_count (ev):
    return events[ev]

# Get DMA physical address.
#
def dma_phys_address ():
    return uio_dma_phys_address()

# Get word from DMA buffer.
#   addr    index into buffer
#
def dma_get_word (addr):
    return uio_dma_get_word(addr)

# Put word into DMA buffer.
#   addr    index into buffer
#   data    data word
#
def dma_put_word (addr, data):
    return uio_dma_put_word(addr, data)

# Load the FPGA configuration from a byte array
#   data    configuration byte array
#
#def configdata_load (data):
#    fpga_configdata_load(data)
