import sys
sys.path.append("/Users/dun202/install/3pl")
import info2py
from cpu_serial import *
import time
import signal
import threading
#import prog_fpga as fpga

fpga_open("/dev/tty.usbserial-A50285BI")

fpga = info2py.info_from_file("prog")   # load info from prog.info file
#fpga = info2py.info_from_fpga()         # load info from FPGA

dmabuffer = bytearray(4096)
DMA_buffer(dmabuffer)

print("\nwrite address 0 to WA")
fpga.WA_write(0)
print("write data to WD");
fpga.WD_write(0x552244aa)
print("wait for DMA");
time.sleep(1.0)     # wait for DMA
print(hex(int.from_bytes(dmabuffer[0:4], byteorder='little', signed=False)) + " (should be 552244aa)")

print("\nwrite address 4*3 to WA")
fpga.WA_write(4*3)
print("write data to WD");
fpga.WD_write(0x31425364)
print("wait for DMA");
time.sleep(1.0)     # wait for DMA
print(hex(int.from_bytes(dmabuffer[4*3:4*3+4], byteorder='little', signed=False)) + " (should be 0x31425364)")

dmabuffer[0:4] = int.to_bytes(0x12345678, 4, byteorder='little', signed=False)
dmabuffer[12:16] = int.to_bytes(0x11335577, 4, byteorder='little', signed=False)

print("\nwrite address 0 to RA")
fpga.RA_write(0)
print("wait for DMA");
time.sleep(1.0)     # wait for DMA
print("read RD");
print("DMA read = " + hex(fpga.RD_read()) + " (should be 0x12345678)")

print("\nwrite address 12 to RA")
fpga.RA_write(12)
print("wait for DMA");
time.sleep(1.0)     # wait for DMA
print("read RD");
print("DMA read = " + hex(fpga.RD_read()) + " (should be 0x11335577)")

fpga_close()

os.kill(os.getpid(), signal.SIGTERM)    # use this to kill all threads!
