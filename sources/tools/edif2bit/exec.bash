# ------------------------------------------------------------------------ EXEC
# $Id: exec.bash 9018 2020-02-04 07:47:33Z kir092 $

# The actual FPGA build script is written in Tcl and executed either in the ISE
# Tcl interpreter (xtclsh) or Vivado, which is also a Tcl interpreter

#echo "INFO: Running ${prog}"

# execute the remainder of this program as a Tcl "here document"
eval exec ${prog} <<'xyzzy'

