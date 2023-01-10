import sys
import os
import time
import signal
import threading
sys.path.append('/Users/dun202/install/3pl')
import cpu_serial
import info2py

portName = "/dev/tty.usbmodem14101"    # Avnet AES board

cpu_serial.fpga_open(portName)

fpga = info2py.info_from_file('prog')


def WQ_handler():
    fpga.WQ_wait()
    print("event WQ")
    exit(0)

def RQ_handler():
    fpga.RQ_wait()
    print("event RQ")
    exit(0)

def EVR_handler():
    fpga.EVR_wait()
    print("event EVR")
    exit(0)


def EVQ_handler():
    fpga.EVQ_wait()
    print("event EVQ")
    exit(0)

print("starting event threads")

twq = threading.Thread(target=WQ_handler, args=())
twq.start()
trq = threading.Thread(target=RQ_handler, args=())
trq.start()
tevr = threading.Thread(target=EVR_handler, args=())
tevr.start()
tevq = threading.Thread(target=EVQ_handler, args=())
tevq.start()

wdata16  = 0x5536
wdata64  = 0x57364925
wdata8   = 0x34
wqdata16 = -2650
wmdata16 = 1234

rdata16 = fpga.R_read()
print(str.format("read {} {} (should be 0x137 first time)", str(rdata16), hex(rdata16)))

fpga.W_write(wdata16)
rdata16 = fpga.R_read()
print(str.format("read {} {} (should be {} {})", str(rdata16), hex(rdata16), str(wdata16), hex(wdata16)))

fpga.WW_write(wdata64)
rdata64 = fpga.RR_read()
print(str.format("read {} (should be {})", hex(rdata64), hex(wdata64)))
    
fpga.WW_write(-137)
rdata64 = fpga.RR_read()
print(str.format("{} (should be -137)", str(rdata64)))


print("should get interrupt EVR")
fpga.WWW_write(wdata8)
rdata8 = fpga.RRR_read()
print(str.format("read {} (should be {})", hex(rdata8), hex(wdata8)))


fpga.RQ_avail_thresh_write(2)
fpga.WQ_avail_thresh_write(14)

rqavail = fpga.RQ_avail_read()
print(str.format("q avail {} (should be 0)", str(rqavail)))
rqdata16 = fpga.RQ_read()
print(str.format("q read {} {} (should be -1 0x3fff)", str(rqdata16), hex(rqdata16)))


print("should get interrupt EVQ")
fpga.WQ_write(wqdata16)

print("should get interrupt EVQ")
print("should get interrupt RQ")
fpga.WQ_write(wqdata16)
rqavail = fpga.RQ_avail_read()
print(str.format("q avail {} (should be 2)", str(rqavail)))

print("should get interrupt EVQ")
fpga.WQ_write(wqdata16)
rqavail = fpga.RQ_avail_read()


rqavail = fpga.RQ_avail_read()
print(str.format("q avail {} (should be 3)", str(rqavail)))
rqdata16 = fpga.RQ_read();
print(str.format("q read {} {} (should be {} {})", str(rqdata16), hex(rqdata16), str(wqdata16), hex(wqdata16)))

rqavail = fpga.RQ_avail_read()
print(str.format("q avail {} (should be 2)", str(rqavail)))
print("should get interrupt WQ");
rqdata16 = fpga.RQ_read();
print(str.format("q read {} {} (should be {} {})", str(rqdata16), hex(rqdata16), str(wqdata16), hex(wqdata16)))

rqavail = fpga.RQ_avail_read()
print(str.format("q avail {} (should be 1)", str(rqavail)))
rqdata16 = fpga.RQ_read();
print(str.format("q read {} {} (should be {} {})", str(rqdata16), hex(rqdata16), str(wqdata16), hex(wqdata16)))
rdata16 = fpga.R_read();

rqavail = fpga.RQ_avail_read();
print(str.format("q avail {} (should be 0)", str(rqavail)))
rqdata16 = fpga.RQ_read();
print(str.format("q read {} (should be -1)", str(rqdata16)))

roq = fpga.RQO_read();
print(str.format("qo read {} (should be -2650)", str(roq)))
roq = fpga.RQO_read();
print(str.format("qo read {} (should be -2650)", str(roq)))

rmdata16 = fpga.MR_read(10)
print(str.format("read {} (should be 30)", str(rmdata16)))
rmdata16 = fpga.MR_read(20)
print(str.format("read {} (should be 60)", str(rmdata16)))

fpga.MW_write(wmdata16, 15)
rmdata16 = fpga.MR_read(15)
print(str.format("q read {} {} (should be {} {})", str(rmdata16), hex(rmdata16), str(wmdata16), hex(wmdata16)))


vs = fpga.SR_read() # vs is struct st2
vss = vs['f4']      # vss is struct st1
vsa = vs['f5']      # vsa is array [2][3]:int:8
vsa0 = vsa[0]       # vsa0 is [3]:int:8
vsa1 = vsa[1]       # vsa1 is [3]:int:8

print("st2.f1 " + str(vs['f1']) + " (511)")
print("st2.f2 " + str(vs['f2']) + " (0)")
print("st2.f3 " + str(vs['f3']) + " (False)")
print("st2.f4.ff1 " + str(vss['ff1']) + " (123)")
print("st2.f4.ff2 " + str(vss['ff2']) + " (True)")
print("st2.f5[0][0] " + hex(vsa0[0]) + " (0x40)")
print("st2.f5[0][1] " + str(vsa0[1]) + " (4)")
print("st2.f5[0][2] " + str(vsa0[2]) + " (7)")
print("st2.f5[1][0] " + hex(vsa1[0]) + " (0)")
print("st2.f5[1][1] " + str(vsa1[1]) + " (1)")
print("st2.f5[1][2] " + str(vsa1[2]) + " (-2)")

vs['f1'] = 321
vs['f2'] = -1234
vs['f3'] = True
vss['ff1'] = 321
vss['ff2'] = False
vsa0[0] = 12
vsa0[1] = 34
vsa0[2] = 56
vsa1[0] = 99
vsa1[1] = -10
vsa1[2] = 88
fpga.SW_write(vs)

vs = fpga.SR_read()
print("st2.f1 " + str(vs['f1']) + " (321)")
print("st2.f2 " + str(vs['f2']) + " (-1234)")
print("st2.f3 " + str(vs['f3']) + " (True)")
print("st2.f4.ff1 " + str(vss['ff1']) + " (123)")
print("st2.f4.ff2 " + str(vss['ff2']) + " (False)")
print("st2.f5[0][0] " + hex(vsa0[0]) + " (12)")
print("st2.f5[0][1] " + str(vsa0[1]) + " (34)")
print("st2.f5[0][2] " + str(vsa0[2]) + " (56)")
print("st2.f5[1][0] " + hex(vsa1[0]) + " (99)")
print("st2.f5[1][1] " + str(vsa1[1]) + " (-10)")
print("st2.f5[1][2] " + str(vsa1[2]) + " (88)")


uf = fpga.UFR_read()
f = fpga.FR_read()
fl = fpga.FLR_read()
print("uf " + str(uf) + " (32.25)")
print("f " + str(f) + " (-40.5)")
print("fl " + str(fl) + " (-1.5)")

fpga.UFW_write(47.25)
fpga.FW_write(20.125)
fpga.FLW_write(7.0)
uf = fpga.UFR_read()
f = fpga.FR_read()
fl = fpga.FLR_read()
print("uf " + str(uf) + " (47.25)")
print("f " + str(f) + " (20.125)")
print("fl " + str(fl) + " (7.0)")

fpga.FW_write(-20.25)
fpga.FLW_write(-7.0)
f = fpga.FR_read()
fl = fpga.FLR_read()
print("f " + str(f) + " (-20.25)")
print("fl " + str(fl) + " (-7.0)")

time.sleep(1.0)
cpu_serial.fpga_close()

os.kill(os.getpid(), signal.SIGTERM)    # use this to kill all threads!
#exit(0)


''' 
    CAN'T KILL INDIVIDUAL THREADS IN PYTHON!
    // Close the interrupt threads.
    pthread_cancel(WQ_thread);
    pthread_cancel(RQ_thread);
    pthread_cancel(EVR_thread);
    pthread_cancel(EVQ_thread);
}
'''
