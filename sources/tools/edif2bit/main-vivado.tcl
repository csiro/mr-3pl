# ----------------------------------------------------------------------- VIVADO
# $Id: main-vivado.tcl 9015 2020-02-04 06:48:50Z kir092 $

proc options_vivado {} {
}

proc main_vivado {} {
    global design part clo

    # read the EDIF netlist file into Vivado
    read_edif $design.edn

    # read the constraints file into Vivado (if there is one)
    if {[file exists ${design}.xdc]} {
	read_xdc -ref ${design} ${design}.xdc
    }

    # place and route the design
    link_design -top ${design} -part $part
    opt_design
    power_opt_design
    place_design
    phys_opt_design
    route_design
    phys_opt_design
    write_checkpoint -force ${design}.dcp

    # emit the reports
    report_timing_summary -file ${design}.tim
    report_io -file ${design}.pad
    report_utilization -file ${design}.xlxs
    report_clock_utilization -file ${design}.clku

    # write bitstream
    write_bitstream -force ${design}.bit
}
