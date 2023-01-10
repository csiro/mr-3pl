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

def B_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(3, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(3, byteorder='little')
    fpga_rw.comms_write(0, 3, 0, wba)

def P_read():
    rba = fpga_rw.comms_read(0, 2, 0, 6)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0xffffffffffff
    if (fval & 0x800000000000) != 0:
         fval = -((fval ^ 0xffffffffffff) + 1)
    return(fval)

def A_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(4, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(4, byteorder='little')
    fpga_rw.comms_write(0, 2, 0, wba)

# event "INT"
def INT_wait():
    fpga_rw.semaphores[0].acquire()

def INT_count():
    return(fpga_rw.event_count(0))
