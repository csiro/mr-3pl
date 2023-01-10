# post-processing recipe for Vivado (in Tcl)

# read the EDIF netlist file into Vivado
read_edif @designName@.edn

# read the constraints file into Vivado (if there is one)
if {[file exists @designName@.xdc]} {
    read_xdc -ref @designName@ @designName@.xdc
}

# place and route the design
link_design -top @designName@ -part @part@
opt_design
power_opt_design
place_design
phys_opt_design
route_design
phys_opt_design
write_checkpoint -force @designName@.dcp

# emit the reports
report_timing_summary -file @designName@.tim
report_io -file @designName@.pad
report_utilization -file @designName@.xlxs
report_clock_utilization -file @designName@.clku

# write bitstream
write_bitstream -force @designName@.bit
