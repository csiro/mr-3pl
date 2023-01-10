import sys
import os
sys.path.append(os.environ['HOME'] + "/install/3pl")
from fpga import *
#import info2py
import prog_fpga as fpga
import time
import signal
import prog_config as config

FPGA_OPEN()

# both the following work
#fpga = info2py.info_from_file("prog")
#fpga = info2py.info_from_fpga()

# both the following work
#FPGA_CONFIGFILE_LOAD("prog.bit.gz")
FPGA_CONFIGDATA_LOAD(config.data, len(config.data))

print(str(fpga.SR_read()) + "\t(21)")

FPGA_CLOSE()

os.kill(os.getpid(), signal.SIGTERM)    # use this to kill all threads!
