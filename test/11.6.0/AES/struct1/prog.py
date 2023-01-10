#! /usr/bin/env python3

import sys
sys.path.append('/Users/dun202/install/include')
import cpu_serial
import prog_if


#portName = findserialdev("tty.usbmodem")
portName = "/dev/tty.usbmodem14201"
cpu_serial.open_cpu_serial(portName, 0)

vs = prog_if.fpga_SR_read()
print("f1 " + str(vs['f1']) + " (511)");
print("f2 " + str(vs['f2']) + " (2047)");
print("f3 " + str(vs['f3']) + " (True)");
print("f4 " + str(vs['f4']) + " (32767)");

vs['f1'] = 321
vs['f2'] = -1234
vs['f3'] = False
vs['f4'] = 54321
prog_if.fpga_SW_write(vs)

vs = prog_if.fpga_SR_read()
print("f1 " + str(vs['f1']) + " (321)");
print("f2 " + str(vs['f2']) + " (-1234)");
print("f3 " + str(vs['f3']) + " (False)");
print("f4 " + str(vs['f4']) + " (54321)");

cpu_serial.close_cpu_serial()

exit(0)
