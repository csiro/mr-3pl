INSTDIR = $(HOME)/install/3pl

BINDIR = $(INSTDIR)/bin
LIBDIR = $(INSTDIR)/lib
PYLIBDIR = $(LIBDIR)/python3

all::

install:: all
	install -d -m 755 $(BINDIR)
	install -c -m 755 info2c $(BINDIR)
	install -d -m 755 $(PYLIBDIR)
	install -c -m 644 info2c_s.py $(PYLIBDIR)
	install -c -m 644 info2c_z.py $(PYLIBDIR)

clean::
