import os
import math
import string
import textwrap
import argparse
import json
import sys
import time
sys.path.append(os.environ['HOME'] + '/install/3pl')
import cpu_serial as fpga_rw

def RR_read():
    rba = fpga_rw.comms_read(0, 3, 0, 5)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x7ffffffff
    if (fval & 0x400000000) != 0:
         fval = -((fval ^ 0x7ffffffff) + 1)
    return(fval)

def RRR_read():
    rba = fpga_rw.comms_read(0, 4, 0, 1)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x3f
    return(fval)

def WQ_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(2, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 5, 0, wba)

def WQ_avail_read():
    rba = fpga_rw.comms_read(0, 5, 0, 1)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    return(val)

def WQ_avail_thresh_write(wdata):
    val = wdata 
    wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 6, 0, wba)

def MR_read(memaddr):
    rba = fpga_rw.comms_read(1, 9, memaddr, 1)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffff
    return(fval)

def MW_write(wdata, memaddr):
    val = wdata 
    wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(1, 8, memaddr, wba)

def WW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(5, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(5, byteorder='little')
    fpga_rw.comms_write(0, 3, 0, wba)

def W_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(2, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 2, 0, wba)

def RQ_read():
    rba = fpga_rw.comms_read(0, 6, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x3fff
    if (fval & 0x2000) != 0:
         fval = -((fval ^ 0x3fff) + 1)
    return(fval)

def RQ_avail_read():
    rba = fpga_rw.comms_read(0, 7, 0, 1)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    return(val)

def RQ_avail_thresh_write(wdata):
    val = wdata 
    wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 7, 0, wba)

def R_read():
    rba = fpga_rw.comms_read(0, 2, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffff
    return(fval)

def RQO_read():
    rba = fpga_rw.comms_read(0, 8, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x3fff
    if (fval & 0x2000) != 0:
         fval = -((fval ^ 0x3fff) + 1)
    return(fval)

def WWW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(1, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 4, 0, wba)

# event "WQ"
def WQ_wait():
    fpga_rw.semaphores[1].acquire()

def WQ_count():
    return(fpga_rw.event_count(1))

# event "RQ"
def RQ_wait():
    fpga_rw.semaphores[2].acquire()

def RQ_count():
    return(fpga_rw.event_count(2))

# event "EVR"
def EVR_wait():
    fpga_rw.semaphores[0].acquire()

def EVR_count():
    return(fpga_rw.event_count(0))

# event "EVQ"
def EVQ_wait():
    fpga_rw.semaphores[3].acquire()

def EVQ_count():
    return(fpga_rw.event_count(3))
