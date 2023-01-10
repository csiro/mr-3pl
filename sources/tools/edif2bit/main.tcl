# ------------------------------------------------------------------------- MAIN
# $Id: main.tcl 9021 2020-02-05 08:10:07Z kir092 $


# elide any initial "-dummy" argument
if {[lindex $argv 0] == "-dummy"} {
    set argv [lreplace $argv 0 0]
}

# register generic command line options
clo tool {enum vivado ise} vivado "Xilinx tool set to use"
clo compress bool 0 "gzip of completed .bit file"
clo info bool 1 "append .info file"
clo version do 0 "Print version and exit"
clo help do 0 "Print usage help and exit"

# register tool options
options_$clo(tool)

# parse command line options
if {[catch {clo_scan $argv}]} {
    clo_usage
    exit 1
}

# version
if {$clo(version)} {
    puts "INFO: edif2bit version $version built $built"
    exit 0
}

# help text
if {$clo(help)} {
    clo_usage
    exit 0
}

# check remnants, which should be the input file
if {[llength $clo(REMNANTS)] != 1} {
    clo_usage "Exactly one input argument required"
    exit 1
}

# extract the design name and directory
set design [file rootname [file tail $clo(REMNANTS)]]
set dir [file dirname $clo(REMNANTS)]

# change to the directory where the source file is
cd $dir

# check the file is actually there
if {! [file readable ${design}.edn]} {
    puts stderr "ERROR: can't read EDIF file ${design}.edn"
    exit 1
}

# get FPGA part from EDIF
set part [part_find $design.edn]
puts "INFO: Part from EDIF file was $part"


# run the tool!
main_$clo(tool)

# if we got a .bit file 
if {[file exists ${design}.bit]} {

    # optionally append .info (or .rpt) file (default is to do so)
    if {$clo(info)} {
	if {[file exists ${design}.info]} {
	    exec echo --------INFO-------- >> ${design}.bit
	    exec cat ${design}.info >> ${design}.bit
	}
    }

    # optionally compress 
    if {$clo(compress)} {
	exec gzip -f ${design}.bit
    }
}
