#! /usr/bin/env python3

import sys
sys.path.append('/Users/dun202/install/include')
sys.path.append('/Users/dun202/install/lib')
import cpu_serial
import threading
import time
import uart_info2py

#portName = findserialdev("tty.usbmodem")
portName = "/dev/tty.usbmodem14201"
cpu_serial.open_cpu_serial(portName, 2)

fpga = uart_info2py.info_from_file('prog')

def event_handler_1():
    fpga.EV1_wait()
    print("event 1")
    exit(0)


def event_handler_2():
    fpga.EV2_wait()
    print("event 2")
    exit(0)

t1 = threading.Thread(target=event_handler_1, args=())
t1.start()
t2 = threading.Thread(target=event_handler_2, args=())
t2.start()

time.sleep(3)
fpga.STARTW_write(True)
time.sleep(2)


cpu_serial.close_cpu_serial()

exit(0)
