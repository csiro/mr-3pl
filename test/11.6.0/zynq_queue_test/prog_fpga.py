import os
import math
import string
import textwrap
import argparse
import json
import sys
import time
sys.path.append(os.environ['HOME'] + '/install/3pl')
import fpga as fpga_rw

def PC_read():
    rba = fpga_rw.comms_read(0, 6, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x3ff
    return(fval)

def PR_read():
    rba = fpga_rw.comms_read(0, 2, 0, 4)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffffffff
    return(fval)

def PR_avail_read():
    rba = fpga_rw.comms_read(0, 3, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    return(val)

def PR_avail_thresh_write(wdata):
    val = wdata 
    wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 4, 0, wba)

def RFRED_read():
    rba = fpga_rw.comms_read(0, 7, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffff
    return(fval)

def PW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(4, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(4, byteorder='little')
    fpga_rw.comms_write(0, 6, 0, wba)

def PW_avail_read():
    rba = fpga_rw.comms_read(0, 4, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    return(val)

def PW_avail_thresh_write(wdata):
    val = wdata 
    wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 8, 0, wba)

def WFRED_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(2, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 10, 0, wba)

def PE_read():
    rba = fpga_rw.comms_read(0, 5, 0, 4)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffffffff
    return(fval)

# event "PR"
def PR_wait():
    fpga_rw.event_wait(0)

def PR_count():
    return(fpga_rw.event_count(0))

# event "PW"
def PW_wait():
    fpga_rw.event_wait(1)

def PW_count():
    return(fpga_rw.event_count(1))
