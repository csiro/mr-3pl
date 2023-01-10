import sys
import os
sys.path.append(os.environ['HOME'] + "/install/3pl")
from fpga import *
#import info2py
import prog_fpga as fpga
import time
import signal
import threading
#import prog_config as config

#fpga_configdata_load(config.data, len(config.data))
fpga_configfile_load("prog.bit")

fpga_open()

#fpga = info2py.info_from_file("prog")   # load info from prog.info file
#fpga = info2py.info_from_fpga()         # load info from FPGA

def INT_handler():
    while True:
        fpga.INT_wait()
        print("event INT")
        print("number of events " + str(fpga.INT_count()))


ev_INT = threading.Thread(target=INT_handler, args=(), daemon=True)
ev_INT.start()

a = 123
b = 1

fpga.A_write(a)
fpga.B_write(b)
print(str(fpga.P_read()) + "\t(should be 123)")
a = 1
fpga.A_write(a);    # will generate an interrupt
a = 2
fpga.A_write(a);
a = 1
fpga.A_write(a);    # will generate an interrupt

time.sleep(1.0)     # wait for the interrupts

fpga_close()

os.kill(os.getpid(), signal.SIGTERM)    # use this to kill all threads!
