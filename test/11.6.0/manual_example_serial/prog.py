import sys
sys.path.append("/Users/dun202/install/3pl")
import info2py
from cpu_serial import *
import time
import signal
import threading
#import prog_fpga as fpga

fpga_open("/dev/tty.usbmodem14101")

#fpga = info2py.info_from_file("prog")   # load info from prog.info file
fpga = info2py.info_from_fpga()         # load info from FPGA

def INT_handler():
    while True:
        fpga.INT_wait()
        print("event INT")

ev_INT = threading.Thread(target=INT_handler, args=(), daemon=True)
ev_INT.start()

a = 123
b = 1

fpga.A_write(a)
fpga.B_write(b)
print(str(fpga.P_read()) + "\t(should be 123)")
a = 1
fpga.A_write(a);    # will generate an interrupt

time.sleep(1.0)     # wait for the interrupt

fpga_close()

os.kill(os.getpid(), signal.SIGTERM)    # use this to kill all threads!
