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

def WA_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(2, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 2, 0, wba)

def WD_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(4, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(4, byteorder='little')
    fpga_rw.comms_write(0, 3, 0, wba)

def RA_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(2, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(2, byteorder='little')
    fpga_rw.comms_write(0, 4, 0, wba)

def RD_read():
    rba = fpga_rw.comms_read(0, 2, 0, 4)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffffffff
    return(fval)
