import sys
sys.path.append("/root/install/3pl")
from fpga import *
import info2py
import time
import signal
import threading

def tohex(val, nbits):
  return hex((val + (1 << nbits)) % (1 << nbits))

FPGA_OPEN()

#fpga = info2py.info_from_file("prog")
fpga = info2py.info_from_fpga()

def I_handler():
    while True:
        fpga.I_wait()
        print("event I")

ev_I = threading.Thread(target=I_handler, args=(), daemon=True)
ev_I.start()

print(hex(fpga.R_read()) + "\t(0x1234)")
fpga.W_write(0x5678);
print(hex(fpga.R_read()) + "\t(0x5678)")
print(hex(fpga.RR_read()) + "\t(0x135798642)")
fpga.WW_write(0x2222222211111111);
print(hex(fpga.RR_read()) + "\t(0x2222222211111111)")

print("Triggering interrupt")
fpga.IW_write(True);
time.sleep(1.0)
print("Triggering interrupt")
fpga.IW_write(True);


time.sleep(2.0)


# DMA write
#
print("\nDMA write")
d = (0x123456789876, 0xabcd1234ef, 0x3311, 0x5522)
a = dma_phys_address();
print("DMA phys address " + hex(a))
fpga.WA_write(a)    # DMA block physical address
fpga.WL_write(4-1)  # transfer 4 64-bit words
for i in range(4):  # write the 4 words to DMA
    fpga.WD_write(d[i])
    
j = 0
for i in range(0, 8, 2):  # read 4 64-bit words from DMA buffer
    print(tohex(dma_get_word(i+1), 32) + " " + tohex(dma_get_word(i), 32) + "\t(" + hex(d[j]) + ")")
    j = j + 1

# DMA read
#
print("\nDMA read")
#for i in range(8):  # write 8 32-bit words to DMA buffer
#    dma_put_word(i + 0x100, 0x5678)
fpga.RA_write(a)    # DMA block physical address
fpga.RL_write(0)  # transfer 1 64-bit word

time.sleep(1.0)

print(hex(fpga.RD_read()) + "\t(0x123456789876)")  # read the DMA'd word

# large write and read - 5 32-bit words, 160 bits
#
print("\nLarge write then read")
bd = (0x12345678, 0xabcd1234, 0x444222, 0x666333, 0x13243546)
fpga.BIGW_write(bd);
rd = fpga.BIGR_read();
for i in range(5):
    print(hex(rd[i]) + "\t(" + hex(bd[i]) + ")")

# Transfer summary
#
'''
print("\nWrite transfer summary")
for i in range(20):
    v = fpga.DIAGW_read()
    if v['len'] == 31:
        break
    print("write addr " + str(v['addr']) + "   len " + str(v['len']))
print("\nRead transfer summary")
for i in range(20):
    v = fpga.DIAGR_read()
    if v['len'] == 31:
        break
    print("read addr " + str(v['addr']) + "   len " + str(v['len']))
'''

FPGA_CLOSE()

os.kill(os.getpid(), signal.SIGTERM)    # use this to kill all threads!
