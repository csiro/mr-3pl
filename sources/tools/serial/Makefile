INSTDIR = $(HOME)/install/3pl

INCDIR = $(INSTDIR)/include
LIBDIR = $(INSTDIR)/lib
PYLIBDIR = $(LIBDIR)/python3

CC = gcc
CFLAGS = -O

all:: cpu_serial.o

install:: all
	install -d -m 755 $(INCDIR)
	install -c -m 644 cpu_serial.h $(INCDIR)
	install -d -m 755 $(LIBDIR)
	install -c -m 644 cpu_serial.o $(LIBDIR)
	install -d -m 755 $(PYLIBDIR)
	install -c -m 644 cpu_serial.py $(PYLIBDIR)

clean::
	rm -f cpu_serial.o
