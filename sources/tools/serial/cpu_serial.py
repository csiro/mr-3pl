import os
import fcntl
import termios
import queue
import threading

tio = None
infile = None
fd = None
q = queue.Queue()
ninterrupts = 19

# Write data to FPGA.
#   mem         0 if static or queue write, 1 if memory write
#   address     FPGA interface address
#   memaddress  memory address if mem == 1
#   dba         byte array of data to send
#
def comms_write (mem, address, memaddress, dba):
    ba = bytearray()
    ba += bytes([0x80 | (mem << 6) | address])          # command
    if mem != 0:
        ba += memaddress.to_bytes(2, byteorder='little')# append memory address
    if dba != None:
        ba += dba                                       # append data
    os.write(fd, ba)                                    # write to FPGA

# Read data from FPGA.
#   mem         0 if value or queue read, 1 if memory read
#   address     FPGA interface address
#   memaddress  memory address if mem == 1
#   bytes       data length - NOT USED HERE (required by Zynq)!
#
def comms_read (mem, address, memaddress, bytecount):
    global q
    wba = bytearray()
    wba += bytes([(mem << 6) | address])                # command - address and memory flag
    if mem != 0:
        wba += memaddress.to_bytes(2, byteorder='little')# append memory address
    os.write(fd, wba)                                   # write to FPGA
    return(q.get())                                     # read response from FPGA
        

# Close the serial port to the FPGA.
def close_port ():
    # Restore terminal settings
    tcsetattr(fd, TCSANOW, tio)
    close(fd)

# Handle input from FPGA via serial port.
def input_handler ():
    global fd
    global q
    while True:
        try:
            r = os.read(fd, 1)
        except OSError:
            print("UART read error")
            exit(1)
        c = r[0]
        if c & 0x80:
            if (c & 0x60) == 0:
                n = c & 0x0f;   # interrupt ID
                # is an interrupt
                if n >= 20:
                    print("interrupt " + str(n) + " illegal (max 19)")
                    exit(1)
                else:
                    semaphores[n].release()
            elif (c & 0x20) != 0:
                # is FPGA -> CPU DMA
                bytes = c & 0x1f;   # byte count
                addr = int.from_bytes(os.read(fd, 2), byteorder='little', signed=False)
                while bytes != 0:
                    dma_buffer[addr] = int.from_bytes(os.read(fd, 1), byteorder='little', signed=False)
                    addr = addr + 1
                    bytes = bytes - 1
            elif (c & 0x40) != 0:
                # is CPU DMA -> FPGA
                # Execute a write to a queue at address 0.
                bytes = c & 0x1f;   # byte count
                addr = int.from_bytes(os.read(fd, 2), byteorder='little', signed=False)
                comms_write(0, 0, 0, dma_buffer[addr:addr+bytes])
        else:
            # is a command return - read the data
            bytes = c & 0x7f    # Note: max of 127 bytes
            buff = os.read(fd, bytes)
            q.put(buff)

# Open the serial port to the FPGA.
def fpga_open (pname):
    global fd
    global tio
    global semaphores
    
    if len(pname) == 0:
        print("could not find USB serial device" + pname)
        exit(1)
    try:
        # Open initially as non-blocking so it does not wait trying to read
        # as it is opened.
        fd = os.open(pname, os.O_RDWR | os.O_NOCTTY | os.O_NONBLOCK)
    except OSError:
        print("Cannot open USB serial device")
        exit(1)

    # Having opened the file and continued, now make the file
    # descriptor blocking.
    flags = fcntl.fcntl(fd, fcntl.F_GETFL)
    flags &= ~os.O_NONBLOCK
    fcntl.fcntl(fd, fcntl.F_SETFL, flags)

    tio = termios.tcgetattr(fd) # get current port settings
    ntio = termios.tcgetattr(fd)
    
    ntio[0] = termios.IGNPAR                # iflags
    ntio[1] = 0                             # oflags - OCANON
    ntio[2] = termios.CS8 | termios.CREAD | termios.CSTOPB | termios.CLOCAL # cflags
    ntio[3] = 0                             # lflags - ICANON
    ntio[4] = termios.B115200               # ispeed
    ntio[5] = termios.B115200               # ospeed
    ntio[6][termios.VMIN] = 1               # blocking read until 1 character arrives
    ntio[6][termios.VSTART] = 0
    ntio[6][termios.VSTOP] = 0
    ntio[6][termios.VINTR] = 0
    ntio[6][termios.VQUIT] = 0
    ntio[6][termios.VSUSP] = 0

    termios.tcflush(fd, termios.TCIFLUSH)
    termios.tcsetattr(fd, termios.TCSANOW, ntio)
    
    # Create and start the input handler thread.
    t = threading.Thread(target=input_handler, args=())
    t.start()
    
    global semaphores
    semaphores = []
    for i in range(ninterrupts):
        semaphores.append(threading.Semaphore(0))

def DMA_buffer (b):
    global dma_buffer
    dma_buffer = b

def fpga_close ():
    close_port()


def open_port (pname):
    if pname == "":
        print("Cannot find USB serial device " + pname)
        exit(1)
    fd = open(pname, 'r')

def close_port ():
    # Restore terminal settings
    termios.tcsetattr(fd, termios.TCSANOW, tio)
    #os.close(fd) HANGS WAITING ON THREAD!
