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

def WW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(8, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(8, byteorder='little')
    fpga_rw.comms_write(0, 6, 0, wba)

def RL_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(1, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 20, 0, wba)

def BIGR_read():
    rba = fpga_rw.comms_read(0, 7, 0, 20)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    v1 = []
    fval = val & 0xffffffff
    val >>= 32
    v1.append(fval)
    fval = val & 0xffffffff
    val >>= 32
    v1.append(fval)
    fval = val & 0xffffffff
    val >>= 32
    v1.append(fval)
    fval = val & 0xffffffff
    val >>= 32
    v1.append(fval)
    fval = val & 0xffffffff
    val >>= 32
    v1.append(fval)
    return(v1)

def BIGW_write(wdata):
    shift = 0
    val = 0
    v2 = wdata[0]
    val = val | ((v2 & 0xffffffff) << shift)
    shift += 32
    v2 = wdata[1]
    val = val | ((v2 & 0xffffffff) << shift)
    shift += 32
    v2 = wdata[2]
    val = val | ((v2 & 0xffffffff) << shift)
    shift += 32
    v2 = wdata[3]
    val = val | ((v2 & 0xffffffff) << shift)
    shift += 32
    v2 = wdata[4]
    val = val | ((v2 & 0xffffffff) << shift)
    shift += 32
    if val < 0:
        wba = val.to_bytes(20, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(20, byteorder='little')
    fpga_rw.comms_write(0, 24, 0, wba)

def RR_read():
    rba = fpga_rw.comms_read(0, 3, 0, 8)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffffffffffffffff
    if (fval & 0x8000000000000000) != 0:
         fval = -((fval ^ 0xffffffffffffffff) + 1)
    return(fval)

def W_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(2, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 4, 0, wba)

def WD_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(8, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(8, byteorder='little')
    fpga_rw.comms_write(0, 14, 0, wba)

def R_read():
    rba = fpga_rw.comms_read(0, 2, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffff
    if (fval & 0x8000) != 0:
         fval = -((fval ^ 0xffff) + 1)
    return(fval)

def WA_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(4, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(4, byteorder='little')
    fpga_rw.comms_write(0, 10, 0, wba)

def RD_read():
    rba = fpga_rw.comms_read(0, 5, 0, 8)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffffffffffffffff
    return(fval)

def RD_avail_read():
    rba = fpga_rw.comms_read(0, 6, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    return(val)

def RD_avail_thresh_write(wdata):
    val = wdata 
    wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 22, 0, wba)

def IW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(1, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 8, 0, wba)

def WL_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(1, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 12, 0, wba)

def RA_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(4, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(4, byteorder='little')
    fpga_rw.comms_write(0, 16, 0, wba)

def RA_avail_read():
    rba = fpga_rw.comms_read(0, 4, 0, 1)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    return(val)

def RA_avail_thresh_write(wdata):
    val = wdata 
    wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 18, 0, wba)

# event "RD"
def RD_wait():
    fpga_rw.event_wait(2)

def RD_count():
    return(fpga_rw.event_count(2))

# event "RA"
def RA_wait():
    fpga_rw.event_wait(1)

def RA_count():
    return(fpga_rw.event_count(1))

# event "I"
def I_wait():
    fpga_rw.event_wait(0)

def I_count():
    return(fpga_rw.event_count(0))
