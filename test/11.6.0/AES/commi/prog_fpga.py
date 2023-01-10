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

def FLW_write(wdata):
    shift = 0
    val = 0
    m, e = math.frexp(wdata)
    if m < 0.0:
        m = -m
        s = 1
    else:
        s = 0
    a = 2 ** 13
    b = a - 2
    m = math.floor(m * a) & b
    c = 2 ** 7 - 1
    e = (e - 1) + c
    val = val | ((m & 0xfff) << shift)
    shift += 12
    val = val | ((e & 0xff) << shift)
    shift += 8
    val = val | ((s & 1) << shift)
    shift += 1
    if val < 0:
        wba = val.to_bytes(3, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(3, byteorder='little')
    fpga_rw.comms_write(0, 12, 0, wba)

def SW_write(wdata):
    shift = 0
    val = 0
    v1 = wdata['f1']
    val = val | ((v1 & 0x1ff) << shift)
    shift += 9
    v1 = wdata['f2']
    val = val | ((v1 & 0xfff) << shift)
    shift += 12
    v1 = wdata['f3']
    val = val | ((v1 & 1) << shift)
    shift += 1
    v1 = wdata['f4']
    v2 = v1['ff1']
    val = val | ((v2 & 0xfff) << shift)
    shift += 12
    v2 = v1['ff2']
    val = val | ((v2 & 1) << shift)
    shift += 1
    v1 = wdata['f5']
    v3 = v1[0]
    v4 = v3[0]
    val = val | ((v4 & 0xff) << shift)
    shift += 8
    v4 = v3[1]
    val = val | ((v4 & 0xff) << shift)
    shift += 8
    v4 = v3[2]
    val = val | ((v4 & 0xff) << shift)
    shift += 8
    v3 = v1[1]
    v5 = v3[0]
    val = val | ((v5 & 0xff) << shift)
    shift += 8
    v5 = v3[1]
    val = val | ((v5 & 0xff) << shift)
    shift += 8
    v5 = v3[2]
    val = val | ((v5 & 0xff) << shift)
    shift += 8
    if val < 0:
        wba = val.to_bytes(11, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(11, byteorder='little')
    fpga_rw.comms_write(0, 9, 0, wba)

def RR_read():
    rba = fpga_rw.comms_read(0, 3, 0, 5)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x7ffffffff
    if (fval & 0x400000000) != 0:
         fval = -((fval ^ 0x7ffffffff) + 1)
    return(fval)

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

def UFR_read():
    rba = fpga_rw.comms_read(0, 11, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xfff
    val >>= 12
    fval /= (1 << 4)
    return(fval)

def RQO_read():
    rba = fpga_rw.comms_read(0, 8, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x3fff
    if (fval & 0x2000) != 0:
         fval = -((fval ^ 0x3fff) + 1)
    return(fval)

def SR_read():
    rba = fpga_rw.comms_read(0, 10, 0, 11)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    v6 = {}
    fval = val & 0x1ff
    val >>= 9
    v6['f1'] = fval
    fval = val & 0xfff
    val >>= 12
    if (fval & 0x800) != 0:
         fval = -((fval ^ 0xfff) + 1)
    v6['f2'] = fval
    fval = val & 1
    val >>= 1
    if fval != 0:
        fval = True
    else:
        fval = False        
    v6['f3'] = fval
    v7 = {}
    fval = val & 0xfff
    val >>= 12
    v7['ff1'] = fval
    fval = val & 1
    val >>= 1
    if fval != 0:
        fval = True
    else:
        fval = False        
    v7['ff2'] = fval
    v6['f4'] = v7
    v8 = []
    v9 = []
    fval = val & 0xff
    val >>= 8
    if (fval & 0x80) != 0:
         fval = -((fval ^ 0xff) + 1)
    v9.append(fval)
    fval = val & 0xff
    val >>= 8
    if (fval & 0x80) != 0:
         fval = -((fval ^ 0xff) + 1)
    v9.append(fval)
    fval = val & 0xff
    val >>= 8
    if (fval & 0x80) != 0:
         fval = -((fval ^ 0xff) + 1)
    v9.append(fval)
    v8.append(v9)
    v10 = []
    fval = val & 0xff
    val >>= 8
    if (fval & 0x80) != 0:
         fval = -((fval ^ 0xff) + 1)
    v10.append(fval)
    fval = val & 0xff
    val >>= 8
    if (fval & 0x80) != 0:
         fval = -((fval ^ 0xff) + 1)
    v10.append(fval)
    fval = val & 0xff
    val >>= 8
    if (fval & 0x80) != 0:
         fval = -((fval ^ 0xff) + 1)
    v10.append(fval)
    v8.append(v10)
    v6['f5'] = v8
    return(v6)

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

def WWW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(1, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 4, 0, wba)

def RRR_read():
    rba = fpga_rw.comms_read(0, 4, 0, 1)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x3f
    return(fval)

def R_read():
    rba = fpga_rw.comms_read(0, 2, 0, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffff
    return(fval)

def UFW_write(wdata):
    shift = 0
    val = 0
    intval =  math.floor(wdata)
    fracval =   math.floor((wdata - intval) * 2 ** 4)
    val = val | ((fracval & 0xf) << shift)
    shift += 4
    val = val | ((intval & 0xff) << shift)
    shift += 8
    if val < 0:
        wba = val.to_bytes(2, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 10, 0, wba)

def FR_read():
    rba = fpga_rw.comms_read(0, 12, 0, 3)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xfffff
    val >>= 20
    if (fval & 0x80000) != 0:
         fval = -((fval ^ 0xfffff) + 1)
    fval /= (1 << 8)
    return(fval)

def MR_read(memaddr):
    rba = fpga_rw.comms_read(1, 9, memaddr, 2)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffff
    return(fval)

def MW_write(wdata, memaddr):
    val = wdata 
    wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(1, 8, memaddr, wba)

def FW_write(wdata):
    shift = 0
    val = 0
    intval =  math.floor(wdata)
    fracval =   math.floor((wdata - intval) * 2 ** 8)
    val = val | ((fracval & 0xff) << shift)
    shift += 8
    val = val | ((intval & 0xfff) << shift)
    shift += 12
    if val < 0:
        wba = val.to_bytes(3, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(3, byteorder='little')
    fpga_rw.comms_write(0, 11, 0, wba)

def FLR_read():
    rba = fpga_rw.comms_read(0, 13, 0, 3)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xfff
    val >>= 12
    mant = fval
    fval = val & 0xff
    val >>= 8
    exp = fval - 127
    if mant != 0 or exp != 0:
        mant = mant + 4096
    fval = val & 0xff
    val >>= 1
    if fval == 0:
        sign = 1
    else:
        sign = -1
    v11 = sign * mant * 2.0 ** exp / 4096
    return(v11)

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
