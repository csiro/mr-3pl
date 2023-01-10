#! /usr/bin/env python3

import sys
import os
import signal
sys.path.append(os.environ['HOME'] + '/install/3pl/include')
import cpu_serial
import prog_fpga


#portName = findserialdev("tty.usbmodem")
portName = "/dev/tty.usbmodem14201"
cpu_serial.open_cpu_serial(portName, 0)

prog_fpga.LW_write(5)
prog_fpga.S1W_write(47)

v = prog_fpga.S2R_read()
print(v)

cpu_serial.close_cpu_serial()

os.kill(os.getpid(), signal.SIGTERM)    # use this to kill all threads!

exit(0)

