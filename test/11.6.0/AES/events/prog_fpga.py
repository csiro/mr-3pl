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

def STARTW_write(wdata):
    val = wdata 
    if val < 0:
        wba = val.to_bytes(1, byteorder='little', signed=True)
    else:
        wba = val.to_bytes(1, byteorder='little')
    fpga_rw.comms_write(0, 2, 0, wba)

# event "EV1"
def EV1_wait():
    fpga_rw.semaphores[0].acquire()

def EV1_count():
    return(fpga_rw.event_count(0))

# event "EV2"
def EV2_wait():
    fpga_rw.semaphores[1].acquire()

def EV2_count():
    return(fpga_rw.event_count(1))
