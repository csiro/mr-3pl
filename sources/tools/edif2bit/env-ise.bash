# -------------------------------------------------------------- ISE ENVIRONMENT
# $Id: env-ise.bash 9015 2020-02-04 06:48:50Z kir092 $

# Work out which version of ISE to run, and set up environment for it

if [ "$tool" = 'ise' ]; then

    # XILINX_ISE indicates which version of ISE to use (default to 14.7i)
    export XILINX_ISE=${XILINX_ISE:-/opt/ise/14.7i}

    export PATH=/bin:/usr/bin

    # check for Linux 32 or 64 bit
    uname=`uname -sm`
    case ${uname} in
	Linux*x86_64)	io=lin64 ; ar=x86_64 ;; 
	Linux*86)	io=lin ; ar=i386 ;;
	*)			
	    echo "ERROR: Platform \"${uname}\" is not supported" 1>&2
	    exit 1
	    ;;
    esac

    # internal path depends on ISE version (it seems)
    # FIXME handle more versions!
    case $XILINX_ISE in
	*10.*) ip=ISE ;;
	*12.*) ip=ISE_DS/ISE ;;
	*14.*) ip=ISE_DS/ISE ;;
	*) 
	    echo "ERROR: ISE version not supported" 1>&2
	    exit 1
	    ;;
    esac

    # set PATH, LD_LIBRARY_PATH and other environment
    # this is a minimal subset of ISE's settings*.sh file(s)

    export PATH=${XILINX_ISE}/${ip}/bin/${io}:$PATH
    export LD_LIBRARY_PATH=${XILINX_ISE}/${ip}/lib/${io}
    export LMC_HOME=${XILINX_ISE}/${ip}/smartmodel/${io}
    export NPX_PLUGIN_PATH=${XILINX_ISE}/${ip}/java/${io}/jre/plugin/${ar}/ns610

    # set up program and arguments to be executed shortly
    # (the -dummy prevents first argument being interpreted as a script file)
    prog="xtclsh -dummy $args"

fi

