# This file forms part of Snark, the CSIRO FPGA development system
# Copyright (c) 2016-17 CSIRO Manufacturing
#
# $Id$

# configuration
makedir ?= ../../../mk
svntop ?= ../../..
svnturl ?= /svn/snark/trunk

include $(makedir)/config.mk

# sources
prog_sources = prog.c

# headers
prog_headers = prog.h

# modules
prog_targets = zynq7 
prog_prependcppflags = -I../../libfpga2
prog_appendcppflags = -std=c11
prog_prependldflags = -pthread

prog_zynq7_cflags = -Og -pthread

# link against locally compiled libfpga (for testing)
prog_zynq7_libs = ../../libfpga2/$(call out-file,libfpga2,zynq7,lib) \
	-lcommon -lz -lm

# directives
programs = prog

# rules
include $(makedir)/rules.mk

# ensure it builds the header files before all
# intermediate (*.o) and dependency files (*.d)
$(foreach t,host $(prog_targets),$(call int-file,prog,$t,obj) $(call dep-file,prog,$t,obj)): $(prog_headers)

# local link to executable
link:
	@ln -s $(call out-file,prog,zynq7,exe)


# fpga building
export XILINX_VIVADO=/opt/Xilinx/Vivado/2016.4

BOARD ?= avnet/microzed_7010
DESIGN = prog
BUILD = build/fpga/$(BOARD)

# use local versions for testing
INFO2C = ../../info2c/info2c
EDIF2BIT = edif2bit
BIN2C = bin2c

fpga: $(BUILD)/$(DESIGN).bit.gz

$(BUILD):
	@mkdir -p $(BUILD)

$(BUILD)/$(DESIGN).edn $(BUILD)/$(DESIGN).info: $(DESIGN).3pl | $(BUILD)
	3pl -r $(DESIGN) -o $(BUILD) -D BOARD=boards/$(BOARD).3pl

$(DESIGN).h: $(BUILD)/$(DESIGN).info | $(BUILD)
	$(INFO2C) --minimum-32bit $(BUILD)/$(DESIGN).info

$(BUILD)/$(DESIGN).bit.gz: $(BUILD)/$(DESIGN).edn | $(BUILD)
	$(EDIF2BIT) -compress $(BUILD)/$(DESIGN)

clean::
	@rm -v -rf build/fpga
	@test -d build && rmdir -v --ignore-fail-on-non-empty build
	@rm -v -f $(prog_headers)
