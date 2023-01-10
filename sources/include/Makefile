# This file is part of the 3PL library
# Copyright (C) 2004-20 CSIRO Australia
# $Id: Makefile 9604 2020-07-06 01:27:13Z dun202 $
#
# Makefile to install 3PL libraries (header files). Requires GNU make.
#
# We copy each file to the install directory, but prepend a standard header
# comment and a setdirective call (incorporating a subversion revision number).
# The latter is carried through to the EDIF file produced by the compiler.

TOP = ../..
include $(TOP)/common.mk

FILES = \
    capture.3pl \
    ctypes.3pl \
    divlib.3pl \
    float.3pl \
    jsontype.3pl \
    multlib.3pl \
    spi.3pl \
    stdlib.3pl \
    uart.3pl \
    uarts.3pl \
    xilinx/clocks.3pl \
    xilinx/common.3pl \
    xilinx/cpu.3pl \
    xilinx/uart_cpu.3pl \
    xilinx/dsp48.3pl \
    xilinx/gtp_dual.3pl \
    xilinx/io.3pl \
    xilinx/mgt.3pl \
    xilinx/ppc405lib.3pl \
    xilinx/ppclib.3pl \
    xilinx/postprocess.3pl \
    xilinx/ps7.3pl \
    xilinx/xc2c.3pl \
    xilinx/xc2s.3pl \
    xilinx/xc2v.3pl \
    xilinx/xc2vp.3pl \
    xilinx/xc3s.3pl \
    xilinx/xc4v.3pl \
    xilinx/xc5v.3pl \
    xilinx/xc6s.3pl \
    xilinx/xc6v.3pl \
    xilinx/xc7.3pl \
    xilinx/xcv.3pl \
    xilinx/xc95.3pl \
    xilinx/xulib.3pl \
    xilinx/zynq_axi.3pl \
    xilinx/vivado.tcl \
    xilinx/ise_fpga.cmd \
    xilinx/ise_cpld.cmd \
    xilinx/ise_xflow.cmd \
    boards/csiro/ddm/dam.3pl \
    boards/csiro/ddm/ddm.3pl \
    boards/csiro/ddm/dbpm.3pl \
    boards/csiro/hymod/clp1.3pl \
    boards/csiro/hymod/ext_ram.3pl \
    boards/csiro/hymod/interrupts.3pl \
    boards/csiro/hymod/io1_interrupts.3pl \
    boards/csiro/hymod/io1.3pl \
    boards/csiro/hymod/io2.3pl \
    boards/csiro/hymod/io2r1.3pl \
    boards/csiro/hymod/io25.3pl \
    boards/csiro/hymod/iocommon1.3pl \
    boards/csiro/hymod/iocommon2.3pl \
    boards/csiro/hymod/iocommon3.3pl \
    boards/csiro/hymod/iocommon4.3pl \
    boards/csiro/hymod/qdr2_ram.3pl \
    boards/csiro/hymod/zbt_ram.3pl \
    boards/csiro/hymod/mezz/hsa.3pl \
    boards/csiro/hymod/mezz/iochan.3pl \
    boards/csiro/hymod/mezz/pulse.3pl \
    boards/csiro/hsc/base.3pl \
    boards/csiro/hsc/mv13.3pl \
    boards/avnet/aes_sp3a_eval400.3pl \
    boards/avnet/microzed_7010.3pl \
    boards/avnet/microzed_7020.3pl \
    boards/bnl/germ_384.3pl \
    boards/dangerous_prototypes/breakout_xc2c64a.3pl \
    boards/dangerous_prototypes/breakout_xc9572xl.3pl \
    boards/digilent/zybo.3pl \
    boards/xilinx/ml505.3pl \
    boards/memec/v4fx12lc.3pl \
    boards/memec/v4fx12mm.3pl \
    boards/gadget_factory/papilio_one_100.3pl \
    boards/gadget_factory/papilio_one_250.3pl \
    boards/gadget_factory/papilio_one_500.3pl \
    boards/gadget_factory/papilio_pro_lx4.3pl \
    boards/gadget_factory/papilio_pro_lx9.3pl \
    boards/fake/virtex6.3pl \
    boards/fake/virtex7.3pl \
    chips/maxim/ds2431.3pl \
    chips/generic/blink.3pl \
    chips/generic/lcd.3pl \
    chips/generic/seven_segment.3pl

# files to not install, for now
IGNORE = \
    mvlib.3pl \
    boards/csiro/hymod/io2p.3pl \
    boards/csiro/hymod2/*

# -------------------- no need to modify below this line --------------------

INCLUDEDIR = $(BUILDDIR)/include
INCLUDEFILES = $(FILES:%=$(INCLUDEDIR)/%)
INCLUDEDIRS = $(sort $(dir $(INCLUDEFILES)))

all: build

build: $(INCLUDEDIRS) $(INCLUDEFILES)

clean:
	@echo + cleaning 3pl include files ...
	@$(call rmfiles,$(INCLUDEFILES))
	@echo + cleaning 3pl include directories ...
	@$(call rmdirs,$(INCLUDEDIRS))

$(INCLUDEDIRS):
	@echo + creating 3pl include directories ...
	@$(call mkdirs,$(INCLUDEDIRS))

$(INCLUDEDIR)/%.3pl: %.3pl HEADER.3pl Makefile
	@echo + creating 3pl include file - $<
	@sed \
	    -e "s?@YEAR@?$(YEAR)?" \
	    -e "s?@NAME@?$<?" \
	    -e "s?@SVNVERSION@?$(SVNVERSION)?" \
	    -e "s?@VERSION@?$(VERSION)$(VERSIONMOD)?" \
	    HEADER.3pl > .new
	@$(call catfile,$<) >> .new
	@$(call cpfile,.new,$@)
	@$(call rmfiles,.new)

$(INCLUDEDIR)/%.tcl: %.tcl HEADER.script Makefile
	@echo + creating script file - $<
	@sed \
	    -e "s?@YEAR@?$(YEAR)?" \
	    -e "s?@NAME@?$<?" \
	    -e "s?@SVNVERSION@?$(SVNVERSION)?" \
	    -e "s?@VERSION@?$(VERSION)$(VERSIONMOD)?" \
	    HEADER.script > .new
	@$(call catfile,$<) >> .new
	@$(call cpfile,.new,$@)
	@$(call rmfiles,.new)

$(INCLUDEDIR)/%.cmd: %.cmd HEADER.script Makefile
	@echo + creating script file - $<
	@sed \
	    -e "s?@YEAR@?$(YEAR)?" \
	    -e "s?@NAME@?$<?" \
	    -e "s?@SVNVERSION@?$(SVNVERSION)?" \
	    -e "s?@VERSION@?$(VERSION)$(VERSIONMOD)?" \
	    HEADER.script > .new
	@$(call catfile,$<) >> .new
	@$(call cpfile,.new,$@)
	@$(call rmfiles,.new)
