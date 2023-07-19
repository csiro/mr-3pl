INSTDIR = $(HOME)/install/3pl


default: edif2bit

install: edif2bit
	install -m 775 edif2bit $(INSTALLDIR)

clean:
	@rm -f edif2bit

COMPONENTS1 = \
	head.bash \
	tool.bash \
	env-ise.bash \
	env-vivado.bash \
	exec.bash 

COMPONENTS2 = \
	clo.tcl \
	part.tcl \
	main-ise.tcl \
	main-vivado.tcl \
	main.tcl \
	foot.bash

edif2bit: $(COMPONENTS1) $(COMPONENTS2) Makefile
	@rm -f edif2bit
	@cat $(COMPONENTS1) \
	    >> edif2bit
	@rev=`git log --no-patch --max-count=1 --pretty=format:%H .` \
	    || rev="unknown"; echo "set version \"$$rev\"" >> edif2bit
	@date '+set built "%Y-%m-%d %H:%M:%S"' \
	    >> edif2bit
	@echo \
	    >> edif2bit
	@cat $(COMPONENTS2) \
	    >> edif2bit
	@chmod a+rx edif2bit
	@echo made script edif2bit

