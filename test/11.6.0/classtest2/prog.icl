Directives - initial values.
----------------------------

	ALU              = false
	FIFO             = false
	OS               = mac os x
	QUEUEREG         = false
	arch             = x86_64
	compileOnly      = false
	compilerMakeDate = 2021-08-06 15:23:59 +1000
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2
	date             = 2021-10-26 14:32:25 +1100
	designName       = prog
	filePath         = false
	forceExec        = true
	gatesNotLuts     = false
	intTruncWarning  = false
	intermediateOnly = false
	listNets         = true
	listTDEs         = true
	listTDEsSel      = 0
	locations        = false
	netFile          = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	unassOut         = fatal
	version          = 11.5.1M (devel svn 9947M, dun202)


3PL version 11.5.1M (devel svn 9947M, dun202).
Source file /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/prog.3pl
command line options - rntfs
2021-10-26 14:32:25 +1100

source files
------------

       prog.3pl
        FMOD1       dummy.3pl
            FMOD2    /Users/dun202/src/mine/3pl/sources/include/xilinx   xc3s.3pl
                SRC      /Users/dun202/src/mine/3pl/sources/include/xilinx   common.3pl
                FMOD3    /Users/dun202/src/mine/3pl/sources/include/xilinx   postprocess.3pl
                FMOD4    /Users/dun202/src/mine/3pl/sources/include   stdlib.3pl
                FMOD5    /Users/dun202/src/mine/3pl/sources/include/xilinx   clocks.3pl
                FMOD6    /Users/dun202/src/mine/3pl/sources/include/xilinx   io.3pl

Directives - final values.
-------------------------

	ALU              = false
	FIFO             = false
	OS               = mac os x
	QUEUEREG         = false
	arch             = x86_64
	compileOnly      = false
	compilerMakeDate = 2021-08-06 15:23:59 +1000
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2
	date             = 2021-10-26 14:32:25 +1100
	designName       = prog
	family           = XC3S
	filePath         = false
	forceExec        = true
	gatesNotLuts     = false
	intTruncWarning  = false
	intermediateOnly = false
	listNets         = true
	listTDEs         = true
	listTDEsSel      = 0
	locations        = false
	netFile          = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/
	part             = xc3s500evq100-4
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.5.1/classtest2/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	srlAddrWidth     = 4
	unassOut         = fatal
	version          = 11.5.1M (devel svn 9947M, dun202)




TDEList after all optimisation
------------------------------


ELEMENT BUFG block e0
	PIN I I prog.FMOD1/dummy.c32_in
	PIN O O glob.c32
NCF "NET PORT_P89 IOSTANDARD=LVCMOS33;" port PORT_P89
IBUF  prog.FMOD1/dummy.c32_in <- PORT_P89 loc=P89 id b2 IBUFG


---------------------------------------------------------------------------
end of TDEList



