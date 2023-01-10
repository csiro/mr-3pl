Directives - initial values.
----------------------------

	ALU              = false
	FIFO             = false
	OS               = mac os x
	QUEUEREG         = false
	arch             = x86_64
	compileOnly      = false
	compilerMakeDate = 2021-08-06 14:54:59 +1000
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest
	date             = 2022-02-11 16:38:37 +1100
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	unassOut         = fatal
	version          = 11.5.1M (devel svn 9947, dun202)


3PL version 11.5.1M (devel svn 9947, dun202).
Source file /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/prog.3pl
command line options - rntfs
2022-02-11 16:38:37 +1100

source files
------------

       prog.3pl
        FMOD1    /Users/dun202/src/mine/3pl/sources/include/boards/avnet   aes_sp3a_eval400.3pl
            FMOD2    /Users/dun202/src/mine/3pl/sources/include/xilinx   xc3s.3pl
                SRC      /Users/dun202/src/mine/3pl/sources/include/xilinx   common.3pl
                FMOD3    /Users/dun202/src/mine/3pl/sources/include/xilinx   postprocess.3pl
                FMOD4    /Users/dun202/src/mine/3pl/sources/include   stdlib.3pl
                FMOD5    /Users/dun202/src/mine/3pl/sources/include/xilinx   clocks.3pl
                FMOD6    /Users/dun202/src/mine/3pl/sources/include/xilinx   io.3pl
            FMOD7    /Users/dun202/src/mine/3pl/sources/include/xilinx   uart_cpu.3pl
                FMOD8    /Users/dun202/src/mine/3pl/sources/include   jsontype.3pl
                FMOD9    /Users/dun202/src/mine/3pl/sources/include   uarts.3pl
                SRC      /Users/dun202/src/mine/3pl/sources/include/xilinx   cpu.3pl

Directives - final values.
-------------------------

	ALU              = false
	FIFO             = false
	OS               = mac os x
	QUEUEREG         = false
	arch             = x86_64
	comms_baud_rate  = 53475
	compileOnly      = false
	compilerMakeDate = 2021-08-06 14:54:59 +1000
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest
	date             = 2022-02-11 16:38:37 +1100
	designName       = prog
	excludeinfo      = true
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/
	part             = xc3s400aft256-4
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.6.0/ilmtest/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	srlAddrWidth     = 4
	uart_cpu         = true
	unassOut         = fatal
	version          = 11.5.1M (devel svn 9947, dun202)




TDEList after all optimisation
------------------------------


NCF "CONFIG VCCAUX="3.3";" port null
ELEMENT DCM block dcm0
	PIN PSCLK C GND
	PIN CLKIN I prog.FMOD1/aes_sp3a_eval400.c16_in
	PIN CLKFB I glob.c16
	PIN RST I GND
	PIN PSINCDEC I GND
	PIN PSEN I GND
	PIN CLK0 O prog.FMOD1/aes_sp3a_eval400.c16_raw
	property CLKIN_PERIOD = 62.50
ELEMENT BUFG block e0
	PIN I I prog.FMOD1/aes_sp3a_eval400.c16_raw
	PIN O O glob.c16
OP E6[0] = ~prog.forvar_161.ILM11_0.s[0]	(unsigned)
DEL SSD9 <- glob.c16.start CLK glob.c16 delay 1
OP E13[0] = ~prog.forvar_161.ILM11_1.s[0]	(unsigned)
NCF "NET PORT_C10 IOSTANDARD=LVCMOS33;" port PORT_C10
NCF "NET %n TNM=C16;" port null
NCF "TIMESPEC TS_C16=PERIOD C16 62.5;" port null
NCF "NET PORT_N1 IOSTANDARD=LVCMOS33;" port PORT_N1
NCF "NET PORT_M1 IOSTANDARD=LVCMOS33;" port PORT_M1
REG
	OUT  prog.forvar_161.ILM11_0.s[0]
	<-
	CLK  glob.c16
	D    E6[0]
	CE   SDD21
	R    GND
    {0x0}
prog.forvar_161.ILM11_0.OUTPUT0[0] = prog.forvar_161.ILM11_0.s[0]
OBUF PORT_N1, null <- OUTPUTBIT18[0] _EN=null loc=N1 id b26 OBUF
OUTPUTBIT18[0] = prog.forvar_161.ILM11_0.OUTPUT0[0] cast - pad
REG
	OUT  prog.forvar_161.ILM11_1.s[0]
	<-
	CLK  glob.c16
	D    E13[0]
	CE   SDD22
	R    GND
    {0x0}
prog.forvar_161.ILM11_1.OUTPUT1[0] = prog.forvar_161.ILM11_1.s[0]
OBUF PORT_M1, null <- OUTPUTBIT20[0] _EN=null loc=M1 id b28 OBUF
OUTPUTBIT20[0] = prog.forvar_161.ILM11_1.OUTPUT1[0] cast - pad
DFF FDRSE {
	OUT      SDD21
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD9
init = R
}
DFF FDRSE {
	OUT      SDD22
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD9
init = R
}
IBUF  prog.FMOD1/aes_sp3a_eval400.c16_in <- PORT_C10 loc=C10 id b2 IBUFG
START { 
	OUT  glob.c16.start
	<-
	IN   VCC
	CLK  glob.c16
}


---------------------------------------------------------------------------
end of TDEList



