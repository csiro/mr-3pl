# ------------------------------------------------------------------------ PART
# $Id: part.tcl 9024 2020-02-06 07:28:33Z kir092 $

# extract part designation from near the end of the EDIF netlist file
# (this is where 3pl usually puts them, anyway)
proc part_find {edif} {

    # it's assumed the file already exists!
    set f [open $edif r]
    seek $f -100 end
    while {[gets $f line] >= 0} {
	if [regexp PART $line] {
	    set q1 [expr [string first "\"" $line] + 1]
	    set q2 [expr [string last "\"" $line] - 1]
	    set part [string range $line $q1 $q2]
	}
    }
    close $f

    return $part
}


# Return a key/value list (family, device, package, speed, temp, extra)
# from a part string, using the partial regular expressions in $part_family
# extended with regular expressions for speed grade, package, etc.
# This is needed because the ISE project setup needs it this way -- it seems
# unable to take the part number directly. 
#
# Unhappily there seems to be at least two formats for the part string,
# one for purchasing and one for Vivado/ISE/etc! And sometimes there are
# additional hyphens.
#
# This is based on UG112 (v3.7) September 2012 with some guesswork/additions
# based on fussing with "partgen" output (especially the "family").
#
# e.g., from XC4VLX60-10FFG668CS2:
#
# 	xc4vlv60	device
# 	-10		speed
# 	ffg668		package
# 	c		temp
# 	s2		extra
#       Virtex-4 LX	family (implied by device)
#
# However, sometimes we see XC4LVX60FFG668-10 specified!  We accept either
# and also allow temp and other trailing things to be missing.

proc part_explode {part} {

    set part [string tolower $part]

    # family and device patterns 
    set families {
        "Virtex"		virtex		xcv\\d+
        "Virtex-E"		virtexe		xcv\\d+e

        "Virtex-II"		virtex2		xc2v\\d+
        "Virtex-II Pro"		virtex2p	xc2vp\\d+
        "Virtex-II Pro X"	virtex2p	xc2vpx\\d+

	"Virtex-4 LX"		virtex4		xc4vlx\\d+
	"Virtex-4 SX"		virtex4		xc4vsx\\d+
	"Virtex-4 FX"		virtex4		xc4vfx\\d+

	"Virtex-5 LX"		virtex5		xc5vlx\\d+
	"Virtex-5 LXT"		virtex5		xc5vlx\\d+t
	"Virtex-5 SXT"		virtex5		xc5vsx\\d+t
	"Virtex-5 TXT"		virtex5		xc5vtx\\d+t
	"Virtex-5 FXT"		virtex5		xc5vfx\\d+t

	"Virtex-6 SXT"		virtex6		xc6vsx\\d+t
	"Virtex-6 HXT"		virtex6		xc6vhx\\d+t
	"Virtex-6 LXT"		virtex6l	xc6vlx\\d+tl
	"Virtex-6 LX"		virtex6l	xc6vlx\\dl

	"Virtex-7"		virtex7		xc7v\\d+t
	"Virtex-7"		virtex7		xc7vx\\d+t
	"Virtex-7"		virtex7		xc7vh\\d+t

	"Kintex-7"		kintex7		xc7k\\d+t
	"Kintex-7 L"		kintex7l	xc7k\\d+tl

	"Artix-7"		artix7		xc7a\\d+t
	"Artix-7 L"		artix7l		xc7a\\d+tl

	"Zynq-7000"		zynq		xc7z\\d+
	"Zynq-7000"		zynq		xc7z\\d+s

        "Spartan"		spartan		xcs\\d+
        "Spartan-XL"		spartanxl	xcs\\d+xl

        "Spartan-II"		spartan2 	xc2s\\d+
        "Spartan-IIE"		spartan2e 	xc2s\\d+e

        "Spartan-3"		spartan3	xc3s\\d+
        "Spartan-3A"		spartan3a 	xc3s\\d+a
        "Spartan-3AN"		spartan3an 	xc3s\\d+an
        "Spartan-3A DSP"	spartan3adsp	xc3sd\\d+a
        "Spartan-3E"		spartan3e 	xc3s\\d+e

        "Spartan-6 LX"		spartan6 	xc6slx\\d+
        "Spartan-6 LXT"		spartan6 	xc6slx\\d+t

        "Spartan-7"		spartan7 	xc7s\\d+

        "CoolRunner (XPLA3)"	xpla3		xcr\\d+xl

        "CoolRunnerTM-II"	xbr		xc2c\\d+
        "CoolRunnerTM-II Auto"	acr2		xc2c\\d+a

        "9500XV"		xc9500		xc95\\d+xv
        "9500XL"		xc9500xl	xc95\\d+xl
        "9500XL Auto"		xa9500xl	xa95\\d+xl
        "9500"			xc9500		xc95\\d+
    } 

    
    # construct partial regex family device patterns alternatives
    set dps {}
    foreach {fn fd dp} $families {
	lappend dps $dp
    }
    set dps [join $dps |]

    # construct complete regex and test (two formats)
    set re1 "^($dps)(-\\d+)-?(\[a-z\]+\\d+)(|c|i)(.*)\$"
    set ok [regexp $re1 $part dummy d s p t x]

    if {! $ok} {
	set re2 "^($dps)(\[a-z\]+\\d+)(-\\d+)(|c|i)(.*)\$"
	set ok [regexp $re2 $part dummy d p s t x]
    }
    if {! $ok} {
	return {}
    }

    # now look up the family, based on $d
    set f ""
    foreach {fn fd dp} $families {
	set re3 "^$dp\$"
	if [regexp $re3 $d] {
	    set n $fn
	    set f $fd
	    break
	}
    }

    # return as list (use "array set" on result)
    return [list \
	familyname $n \
	family $f \
	device $d \
	package $p \
	speed $s \
	temp $t \
	extra $x]
}


# test code for [part_explode]
proc part_test {} {

    foreach part {
        xc3s500evq100-4
	WRONG
	XC5VLX110-1FFG676C
	XC5VLX330T-1FF1738I
	XC5VSX35T-2FF665C
	XC4VLX25-10FF668C
	XC4VSX55-11FF1148C
	XC2VP7-7FG456C
	XC2V1000-5FG456C
	XCV300E-6PQ240C
	XCV300-6PQ240C
	XC3S1000-4FG676C
	XC3S50A-4FTG256C
	XC3S250E-4FT256C
	XC2S50-6PQ208C
	XC2S50E-6PQ208C
	XC3S400AN-4FG400I
	XC3SD1800A-4CS484LI
	XCS20-4PQ208C
	XCS20XL-4PQ208C
	XC4013E-3HQ240C
	XC4013XL-3PQ208C
	XC2C256-7PQ208C
	XCR3512XL-7PQ208C
	XC9536XV-7VQ44I
	XC9572XL-7TQ100C
	XC95216-10HQ208C
    } {
	puts -nonewline [format "%-20s " $part]
	set pl [part_explode $part]
	if {$pl == ""} {
	    puts "FAILED!"
	} else {
	    array set pa $pl
	    puts [format "%-20s %-16s %-10s %-3d %s" \
		$pa(familyname) $pa(device) $pa(package) $pa(speed) $pa(temp)]
	}
    }
    return 0
}

#puts [part_explode XC5VLX110-1FFG676C]
#puts [part_explode xc3s500evq100-4]
#part_test
