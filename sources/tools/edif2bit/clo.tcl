# -------------------------------------------------------------------------- CLO
# $Id: clo.tcl 9015 2020-02-04 06:48:50Z kir092 $
#
# clo <name> <type> <default> <description>
#    Add an option to the list of options scanned for later,
#    and set the option value to the <default> value.
#    <type> is one of
#	bool
#	do
#	file
#	host
#	string
#	{int <min> <max>}
#	{enum <val> <val> ...}
#
# clo_scan <argv>
#     Scan <argv> for previously registered options. Returns number
#     of unrecognised options/values (hopefully 0)
#
# clo_usage <message>
#     Print the <message> and the options list and exit
#
# The option value is placed in $clo(<name>). If the special -- option
# is used, all arguments following it will not be interpreted but placed
# in $clo(ARGS). Unrecognised options are placed in $clo(REMNANTS)

proc _clo_find {opt} {
    #
    # search $clo(LIST) for an option info list matching $opt, and
    # return the option linfo list, or empty string if no match
    # (internal)
    global clo 

    # options 
    if [string match -* $opt] {

	# compare against list
	foreach maybe $clo(LIST) {

	    # ordinary options, including booleans
	    if [string match -[lindex $maybe 0] $opt]  {
		 return $maybe
	    }

	    # inverted booleans, i.e., -no-xxx
	    if {
		([lindex [lindex $maybe 1] 0] == "bool") && \
		[string match -no-[lindex $maybe 0] $opt]
	    } {
		 return $maybe
	    }
	}

	# leftovers are probably errors
	clo_usage "unknown option $opt"
    }

    return {}
}


proc _clo_handle {optinfo arg} {
    #
    # handle an option $arg according to the $optinfo entry, put
    # the result in $clo(opt)
    # (internal)
    global clo

    #debug 2 "-[lindex $optinfo 0] is $arg"
    set clo([lindex $optinfo 0]) $arg
}


proc clo_scan {oargv} {
    #
    # Called after all the options have been registered using clo_add.
    # Destructively parses the option list in $oargv using the option list
    # (which is in $clo(LIST)) and sets elements of $clo to their
    # respective values. Any elements in $oargv at the end of parsing
    # are placed in $clo(REMNANTS), except for arguments following the
    # special -- option, which go into $clo(ARGS)
    # 
    global clo 

    set clo(ARGS) {}
    set clo(REMNANTS) {}

    # destructively parse copy of $argv list
    while {[llength $oargv] > 0} {

	# remove the option, and find a match in $clo(LIST)
        set opt [lindex $oargv 0]
	set oargv [lreplace $oargv 0 0]
	set optinfo [_clo_find $opt]

	# check for the special -- option
	if {$opt == "--"} {
	    # just return remaining arguments
	    set $clo(ARGS) $oargv
	    return [llength $clo(REMNANTS)]
	}

	# check the match result
	if {[llength $optinfo] == 0} {
	    # no match for this option, put it in remnant list
	    lappend clo(REMNANTS) $opt
	    continue
	}

	# now get the argument, if any
	if {[lindex $optinfo 1] == "bool"} {

	    # booleans take no extra argument, but have two forms
	    if [string match -no-* $opt] {
		set arg 0
	    } else {
		set arg 1
	    }

	} elseif {[lindex $optinfo 1] == "do"} {

	    # do's take no extra argument
	    set arg 1

	} else {
	    # if there is an =, it's the -option=arg style
	    if {![regexp {^-[^=]+=(.+)$} $opt dummy arg]} {

		# otherwise argument should be the next token
		if [llength $oargv] {
		    set arg [lindex $oargv 0]
		    set oargv [lreplace $oargv 0 0]
		} else {
		    # run out of arguments
		    clo_usage "$opt: missing argument"
		}
	    }
	}

	# process the option and its argument according to type 
	_clo_handle $optinfo $arg
    }

    return [llength $clo(REMNANTS)]
}


proc clo {name type default description} {
    #
    # Add the given option details to the option list, and
    # initialise the option value. This should check for duplicated
    # options, defaults that are out of range, bad type, and so on
    global clo

    lappend clo(LIST) [list $name $type $description]
    _clo_handle [list $name $type $description] $default
}


proc clo_usage {{message {}}} {
    #
    # print a message, usage list and exit
    global clo 

    if [string length $message] {
	puts stderr "ERROR: $message"
    }
    puts stderr "INFO: options:"
    foreach optinfo $clo(LIST) {
        # depends on type
	set t [lindex $optinfo 1]
	set o [lindex $optinfo 0]
        switch [lindex $t 0] {
	    bool {
	        set a {}
	        set d "Switch on/off "
		set o $o/-no-$o
	    }
	    do {
	        set a {}
	        set d ""
	    }
	    int {
	        set a <int>
	        set d "Set "
	    }
	    hexint {
	        set a <hexint>
	        set d "Set "
	    }
	    directory {
	        set a <directory>
	        set d "Set "
	    }
	    file {
	        set a <filename>
	        set d "Set "
	    }
	    string {
	        set a <string>
	        set d "Set "
	    }
	    enum {
	        set a [join [lrange $t 1 end] |]
	        set d "Select "
	    }
	    host {
	        set a <host>
	        set d "Set "
	    }
	}

	# print the option
        puts stderr \
	    [format "    %-30s %s%s" "-$o $a" $d [lindex $optinfo 2]]
    }
}

