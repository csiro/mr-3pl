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

def SR_read():
    rba = fpga_rw.comms_read(0, 2, 0, 5)
    val = int.from_bytes(rba, byteorder='little', signed=False)
    v1 = {}
    fval = val & 0x1ff
    val >>= 9
    v1['f1'] = fval
    fval = val & 0xfff
    val >>= 12
    if (fval & 0x800) != 0:
         fval = -((fval ^ 0xfff) + 1)
    v1['f2'] = fval
    fval = val & 1
    val >>= 1
    if fval != 0:
        fval = True
    else:
        fval = False        
    v1['f3'] = fval
    fval = val & 0xffff
    val >>= 16
    v1['f4'] = fval
    return(v1)

def SW_write(wdata):
    shift = 0
    val = 0
    v2 = wdata['f1']
    val = val | ((v2 & 0x1ff) << shift)
    shift += 9
    v2 = wdata['f2']
    val = val | ((v2 & 0xfff) << shift)
    shift += 12
    v2 = wdata['f3']
    val = val | ((v2 & 1) << shift)
    shift += 1
    v2 = wdata['f4']
    val = val | ((v2 & 0xffff) << shift)
    shift += 16
    if val < 0:
        wba = val.to_bytes(5, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(5, byteorder='little')
    fpga_rw.comms_write(0, 2, 0, wba)
