INSTDIR = $(HOME)/install/3pl

BINDIR = $(INSTDIR)/bin
INCDIR = $(INSTDIR)/include
LIBDIR = $(INSTDIR)/lib
PYLIBDIR = $(LIBDIR)/python3

EXT := fpga_read_write$(shell python3 -c "from distutils import sysconfig;\
	print(sysconfig.get_config_var('EXT_SUFFIX'))")

CC = gcc
CFLAGS = -O

all:: fpga.o $(EXT)

fpga.o: fpga.h

$(EXT): fpga_py_extension.c fpga.c
	python3 setup.py build_ext --inplace

install:: all
	install -d -m 755 $(INCDIR)
	install -c -m 644 fpga.h $(INCDIR)
	install -d -m 755 $(LIBDIR)
	install -c -m 644 fpga.o $(LIBDIR)
	install -d -m 755 $(PYLIBDIR)
	install -c -m 644 $(EXT) fpga.py $(PYLIBDIR)

clean::
	rm -rf build $(EXT) fpga.o
