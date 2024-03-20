TOPTARGETS = all install clean

SUBDIRS = bin2c bin2py edif2bit info2c info2py run_ISE run_Vivado serial zynq

$(TOPTARGETS):: $(SUBDIRS)

$(SUBDIRS)::
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)
