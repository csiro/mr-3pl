# ------------------------------------------------------------------------- TOOL
# $Id: tool.bash 9018 2020-02-04 07:47:33Z kir092 $

# try and work out which tool set to use (Vivado or ISE) based on (a) existence
# of XILINX_* environment variables, or (b) a -tool option (other options are
# ignored, for now). 

tool=none
args=$*

# take a hint from environment
if [ "${XILINX_ISE:x}" != "x" ]; then
    tool='ise'
fi
if [ "${XILINX_VIVADO:x}" != "x" ]; then
    tool='vivado'
fi

# then search command line arguments
while (( "$#" )) ; do
    case "$1" in
	-tool)
	    tool=$2
	    shift 2
	    ;;
	*)
	    shift
	    ;;
    esac
done

# check the tool
case $tool in
    ise|vivado)
	;;
    none)
	echo "ERROR: Missing toolset (use -tool option)" 1>&2
	exit 1
	;;
    *)
	echo "ERROR: Toolset \"${tool}\" is not supported" 1>&2
	exit 1
	;;
esac

