# -------------------------------------------------------------------------- ISE
# $Id: main-ise.tcl 9022 2020-02-05 12:14:13Z kir092 $

proc options_ise {} {
}


proc main_ise {} {
    global design part clo

    # removed any previous project
    if [file exists ${design}.xise] {
	puts "INFO: Removing existing project ${design}.xise"
	project open ${design}.xise
	project clean
	project close
	file delete ${design}.xise
    } 
    project new ${design}.xise

    # set project-level properties
    set pl [part_explode $part]
    if {$pl == {}} {
	puts stderr "ERROR: Failed to explode $part"
	exit 1
    }
    array set pa $pl
   
    project set family $pa(family)
    project set device $pa(device)
    project set package $pa(package)
    project set speed $pa(speed)

    # add all the source HDLs and ucf
    foreach ext {edn ucf ncf} {
	if {[file exists ${design}.${ext}]} {
	    xfile add ${design}.${ext}
	}
    }

    # set batch application options :
    # 1. Set synthesis optimization goal to speed
    # 2. Ignore any LOCs in ngdbuild
    # 3. Perform timing-driven packing
    # 4. Use the highest par effort level
    # 5. Set the par extra effort level
    # 6. Pass "-instyle xflow" to the par command-line
    # 7. Generate a verbose report from trce
    # 8. Create the IEEE 1532 file during bitgen
    project set "Optimization Goal" Speed
    project set "Use LOC Constraints" false
    project set "Place & Route Effort Level (Overall)" High
    project set "Extra Effort (Highest PAR level only)" "Continue on Impossible"
    project set "Report Type" "Verbose Report" \
	-process "Generate Post-Place & Route Static Timing"

    # run the entire xst-to-trce flow
    process run "Generate Programming File"
    project close

    # FIXME somehow check for success

    # re-open and archive project
    set ts [clock format [file mtime ${design}.xise] -format "%Y%m%d-%H%M%S"]
    set arch "${design}-$ts.zip"
    project open ${design}.xise
    puts "INFO: Archiving project to ${arch}"
    project archive ${arch}
    project close

    return 0
}


# this is the traditional Makefile sequence, for reference
proc main_ise_old {} {
    global design part clo

    proc run {args} {
	puts "INFO: Running $args"
	eval exec $args
    }

    # naive execution of command-line programs
    run  ngdbuild ${design}.edn
    run  map -detail -w -timing -ol high -pr b ${design}
    run  par -w -ol high ${design} ${design}.ncd
    run  bitgen -m -w -g LCK_cycle:4 -g GTS_cycle:1 ${design}

    return 0
}
