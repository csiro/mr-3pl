# ----------------------------------------------------------- VIVADO ENVIRONMENT
# $Id: env-vivado.bash 9023 2020-02-06 07:26:25Z kir092 $

# Work out which version of Vivado to run, and set up execution environment

if [ "${tool}" = 'vivado' ]; then

    # XILINX_VIVADO indicates which version of Vivado to use (default to 2016.1)
    export XILINX_VIVADO=${XILINX_VIVADO:-/opt/Xilinx/Vivado/2016.1}

    # set PATH
    export PATH=${XILINX_VIVADO}/bin:/bin:/usr/bin

    # set up program and arguments to be executed shortly
    prog="vivado -mode tcl -nojournal -nolog -tclargs ${args}"

fi

