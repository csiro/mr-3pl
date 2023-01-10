#from enum import Flag, auto
import os
import sys

REG_UIO_NAME = "axi_mgp0"
MEM_UIO_NAME = "axi_mgp1"
DMA_UIO_NAME = "axi_hpbuf"


# search all UIO devices and find one with the given name
# and returns its unit number
def uio_find(name):
    print("find " + name)
    for filename in os.listdir("/sys/class/uio"):
        unit = filename[3:]
        ufile = open("/sys/class/uio/" + filename + "/name", 'r')
        uname = ufile.readline()[0:-1]
        print("found uname " + uname + " length " + str(len(uname)))
        if uname == name:
            ufile.close()
            return unit
    print("UIO name " + name + " not found")
    sys.exit()

print(uio_find(MEM_UIO_NAME))
