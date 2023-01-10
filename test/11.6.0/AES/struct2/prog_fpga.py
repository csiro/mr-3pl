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
    rba = fpga_rw.comms_read(0, 2, 0, 11)
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
    v2 = {}
    fval = val & 0xfff
    val >>= 12
    v2['ff1'] = fval
    fval = val & 1
    val >>= 1
    if fval != 0:
        fval = True
    else:
        fval = False        
    v2['ff2'] = fval
    v1['f4'] = v2
    v3 = []
    fval = val & 0xffff
    val >>= 16
    v3.append(fval)
    fval = val & 0xffff
    val >>= 16
    v3.append(fval)
    fval = val & 0xffff
    val >>= 16
    v3.append(fval)
    v1['f5'] = v3
    return(v1)

def SW_write(wdata):
    shift = 0
    val = 0
    v4 = wdata['f1']
    val = val | ((v4 & 0x1ff) << shift)
    shift += 9
    v4 = wdata['f2']
    val = val | ((v4 & 0xfff) << shift)
    shift += 12
    v4 = wdata['f3']
    val = val | ((v4 & 1) << shift)
    shift += 1
    v4 = wdata['f4']
    v5 = v4['ff1']
    val = val | ((v5 & 0xfff) << shift)
    shift += 12
    v5 = v4['ff2']
    val = val | ((v5 & 1) << shift)
    shift += 1
    v4 = wdata['f5']
    v6 = v4[0]
    val = val | ((v6 & 0xffff) << shift)
    shift += 16
    v6 = v4[1]
    val = val | ((v6 & 0xffff) << shift)
    shift += 16
    v6 = v4[2]
    val = val | ((v6 & 0xffff) << shift)
    shift += 16
    if val < 0:
        wba = val.to_bytes(11, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(11, byteorder='little')
    fpga_rw.comms_write(0, 2, 0, wba)
