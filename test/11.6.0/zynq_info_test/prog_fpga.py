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

def SW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(1, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 4, 0, wba)

def SR_read():
    rba = fpga_rw.comms_read(0, 2, 0, 1)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    fval = val & 0x1f
    return(fval)
