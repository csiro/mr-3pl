Directives - initial values.
----------------------------

	ALU              = false
	FIFO             = false
	OS               = mac os x
	QUEUEREG         = false
	arch             = x86_64
	compileOnly      = false
	compilerMakeDate = 2022-11-25 11:59:13 +1100
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi
	date             = 2023-01-13 15:27:50 +1100
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
	netFile          = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/
	parentDirectory  = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	unassOut         = fatal
	version          = 11.7.0M (devel svn 10083:10182M, dun202)


3PL version 11.7.0M (devel svn 10083:10182M, dun202).
Source file /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/prog.3pl
command line options - rntfs
2023-01-13 15:27:50 +1100

source files
------------

       prog.3pl
        FMOD1    /Users/dun202/src/mine/3pl/sources/include/boards/gadget_factory   papilio_one_500.3pl
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
	comms_baud_rate  = 115200
	compileOnly      = false
	compilerMakeDate = 2022-11-25 11:59:13 +1100
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi
	date             = 2023-01-13 15:27:50 +1100
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
	netFile          = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/
	parentDirectory  = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/
	part             = xc3s500evq100-4
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL_distr/3pl/test/11.6.0/papilio/commi/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	srlAddrWidth     = 4
	tlimit           = 1016
	uart_cpu         = true
	unassOut         = fatal
	version          = 11.7.0M (devel svn 10083:10182M, dun202)




TDEList after all optimisation
------------------------------


ELEMENT DCM block dcm0
	PIN PSCLK C GND
	PIN CLKIN I prog.FMOD1/papilio_one_500.c32_in
	PIN CLKFB I glob.c32
	PIN RST I GND
	PIN PSINCDEC I GND
	PIN PSEN I GND
	PIN CLK0 O prog.FMOD1/papilio_one_500.simpleclock_0.craw
	property CLKIN_PERIOD = 31.25
ELEMENT BUFG block e0
	PIN I I prog.FMOD1/papilio_one_500.simpleclock_0.craw
	PIN O O glob.c32
prog.read_1.case_15.read_value_1.in_1[0..15] = prog.r[0..15]
prog.read_2.case_18.read_value_2.in_1[0..34] = prog.rr[0..34]
prog.read_3.case_21.read_value_3.in_1[0..5] = prog.rrr[0..5]
prog.receive_event_0.sig_1[0] = prog.rrra[0]
prog.receive_event_0.sigint_0.sig_1[0] = prog.receive_event_0.sig_1[0]
DEL F11 <- TS5 CLK glob.c32 delay 1
prog.receive_event_0.sigint_0.par_0.seq_0.wait_while_0.v_1[0] = prog.receive_event_0.sigint_0.sig_1[0]
DEL FB18 <- SB17 CLK glob.c32 delay 1
WHILE {
	START_B    SB17
	FINISH     F21
	<-
	START      F11
	TEST       prog.receive_event_0.sigint_0.par_0.seq_0.wait_while_0.v_1[0]
	CONTIN     FB18
	C          glob.c32
	RESET      null
}
AND29[0] = prog.receive_event_0.sigint_0.sig_1[0] AND prog.receive_event_0.sigint_0.q.NF
WHEN {
	T_START  TS5
	F_START  FS6
	FINISH   F4
	<-
	START    S3
	TEST     AND29[0]
	T_FINISH F21
	F_FINISH FF7
}
DEL FF7 <- FS6 CLK glob.c32 delay 1
ILOOP  S3 <- SSD1945 F4
DFF FDRSE {
	OUT      W31[0]
	<-
	D        TS1274
	C        glob.c32
	CE       VCC
	R        GND
	S        GND
init = R
}
DEL F40 <- TS34 CLK glob.c32 delay 1
DEL FB47 <- SB46 CLK glob.c32 delay 1
WHILE {
	START_B    SB46
	FINISH     F50
	<-
	START      F40
	TEST       W31[0]
	CONTIN     FB47
	C          glob.c32
	RESET      null
}
AND58[0] = W31[0] AND prog.receive_event_1.sigint_1.q.NF
WHEN {
	T_START  TS34
	F_START  FS35
	FINISH   F33
	<-
	START    S32
	TEST     AND58[0]
	T_FINISH F50
	F_FINISH FF36
}
DEL FF36 <- FS35 CLK glob.c32 delay 1
ILOOP  S32 <- SSD1945 F33
EXECP no priority, buffered queues only {
	START_DEL SD70
	<-
	CLK       glob.c32
	START_IN  S60
	BQAV      prog.qo.NE
}
DEL F62 <- SD70 CLK glob.c32 delay 1
ILOOP  S60 <- SSD1945 F62
OP E75[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..11] >> 11	(unsigned, unsigned)
E76[0] = E75[0..11] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0] = E76[0]
OP E83[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.prev[0]	(unsigned)
E84[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0] AND E83[0]
OP E88[0..12] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..11] + 235	(unsigned, unsigned)
S91[0..11] = E88[0..12] cast - pad
OP E97[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NF	(unsigned)
OP E107[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3] + 1	(unsigned, unsigned)
S110[0..3] = E107[0..4] cast - pad
WHEN {
	T_START  TS100
	F_START  FS101
	FINISH   -
	<-
	START    SDD2717
	TEST     E84[0]
	T_FINISH -
	F_FINISH -
}
OP E129[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3] == 0	(unsigned, unsigned)
E130[0] = E84[0] AND E129[0]
OP E136[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]	(unsigned)
E137[0] = E136[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16_prev[0]
DEL F141 <- TS133 CLK glob.c32 delay 1
OP E144[0] = ~E130[0]	(unsigned)
DEL FB145 <- SB143 CLK glob.c32 delay 1
WHILE {
	START_B    SB143
	FINISH     F148
	<-
	START      F141
	TEST       E144[0]
	CONTIN     FB145
	C          glob.c32
	RESET      null
}
DEL F154 <- F148 CLK glob.c32 delay 1
OP E157[0] = ~E130[0]	(unsigned)
DEL FB158 <- SB156 CLK glob.c32 delay 1
WHILE {
	START_B    SB156
	FINISH     F161
	<-
	START      F154
	TEST       E157[0]
	CONTIN     FB158
	C          glob.c32
	RESET      null
}
DEL F167 <- F161 CLK glob.c32 delay 1
OP E170[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E173[0] = ~E130[0]	(unsigned)
DEL FB174 <- SB172 CLK glob.c32 delay 1
WHILE {
	START_B    SB172
	FINISH     F177
	<-
	START      SB169
	TEST       E173[0]
	CONTIN     FB174
	C          glob.c32
	RESET      null
}
OP E185[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7] << 1	(unsigned, unsigned)
E186[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0] cast - pad
OP E187[0..8] = E185[0..8] | E186[0]	(unsigned, unsigned)
S190[0..7] = E187[0..8] cast - pad
DEL F184 <- F177 CLK glob.c32 delay 1
OP E194[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S197[0..3] = E194[0..4] cast - pad
WHILE {
	START_B    SB169
	FINISH     F203
	<-
	START      F167
	TEST       E170[0]
	CONTIN     F184
	C          glob.c32
	RESET      null
}
OP E209[0] = ~E130[0]	(unsigned)
DEL FB210 <- SB208 CLK glob.c32 delay 1
WHILE {
	START_B    SB208
	FINISH     F213
	<-
	START      F203
	TEST       E209[0]
	CONTIN     FB210
	C          glob.c32
	RESET      null
}
OP E220[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]	(unsigned)
DEL F219 <- F213 CLK glob.c32 delay 1
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.in_1[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7]
E235[0,1,2,3,4,5,6,7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.in_1[0..7] cast - pad
S236[0] = E235[0] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[0] = S236[0]
S237[0] = E235[1] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[1] = S237[0]
S238[0] = E235[2] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[2] = S238[0]
S239[0] = E235[3] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[3] = S239[0]
S240[0] = E235[4] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[4] = S240[0]
S241[0] = E235[5] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[5] = S241[0]
S242[0] = E235[6] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[6] = S242[0]
S243[0] = E235[7] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[7] = S243[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_2[1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[6]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_3[2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[5]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_4[3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_5[4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[3]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_6[5] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[2]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_7[6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[1]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_8[7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_1[0] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_2[1] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_3[2] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_4[3] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_5[4] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[5] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_6[5] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_7[6] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_8[7] cast - pad
E268[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[0,1,2,3,4,5,6,7] cast - pad
E269[0..7] = E268[0..7] cast - pad
DEL F224 <- F219 CLK glob.c32 delay 1
WHEN {
	T_START  TS225
	F_START  FS226
	FINISH   -
	<-
	START    F219
	TEST     prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NF
	T_FINISH -
	F_FINISH -
}
DEL FF280 <- FS134 CLK glob.c32 delay 1
WHEN {
	T_START  TS133
	F_START  FS134
	FINISH   F132
	<-
	START    S131
	TEST     E137[0]
	T_FINISH F224
	F_FINISH FF280
}
ILOOP  S131 <- SSD1945 F132
E283[0..7] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx[0..7] cast - pad
DEL E290[0] <- E290[0] CLK glob.c32 delay 16 EN E84[0] initialised
E291[0] = E290[0] AND E84[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.tx_out_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.txd_[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.tx_out_2[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.txd_[0]
OP E305[0] = ~E291[0]	(unsigned)
DEL FB308 <- SB307 CLK glob.c32 delay 1
WHILE {
	START_B    SB307
	FINISH     F311
	<-
	START      TS297
	TEST       E305[0]
	CONTIN     FB308
	C          glob.c32
	RESET      null
}
DEL F317 <- SD341 CLK glob.c32 delay 1
EXECP no priority, buffered queues only {
	START_DEL SD341
	<-
	CLK       glob.c32
	START_IN  F311
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NE
}
OP E344[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E345[0] = ~E291[0]	(unsigned)
DEL FB348 <- SB347 CLK glob.c32 delay 1
WHILE {
	START_B    SB347
	FINISH     F351
	<-
	START      SB343
	TEST       E345[0]
	CONTIN     FB348
	C          glob.c32
	RESET      null
}
E358[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.data[0..7] cast - pad
DEL F357 <- F351 CLK glob.c32 delay 1
OP E364[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.data[0..7] >> 1	(unsigned, unsigned)
DEL F363 <- F357 CLK glob.c32 delay 1
OP E370[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S373[0..3] = E370[0..4] cast - pad
DEL F369 <- F363 CLK glob.c32 delay 1
WHILE {
	START_B    SB343
	FINISH     F377
	<-
	START      F317
	TEST       E344[0]
	CONTIN     F369
	C          glob.c32
	RESET      null
}
OP E381[0] = ~E291[0]	(unsigned)
DEL FB384 <- SB383 CLK glob.c32 delay 1
WHILE {
	START_B    SB383
	FINISH     F387
	<-
	START      F377
	TEST       E381[0]
	CONTIN     FB384
	C          glob.c32
	RESET      null
}
DEL F393 <- F387 CLK glob.c32 delay 1
OP E395[0] = ~E291[0]	(unsigned)
DEL FB398 <- SB397 CLK glob.c32 delay 1
WHILE {
	START_B    SB397
	FINISH     F401
	<-
	START      F393
	TEST       E395[0]
	CONTIN     FB398
	C          glob.c32
	RESET      null
}
DEL F407 <- F401 CLK glob.c32 delay 1
DEL F411 <- FS298 CLK glob.c32 delay 1
WHEN {
	T_START  TS297
	F_START  FS298
	FINISH   F296
	<-
	START    S295
	TEST     prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NE
	T_FINISH F407
	F_FINISH F411
}
ILOOP  S295 <- SSD1945 F296
E422[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0] AND prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
E427[0..5,6..7] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7] cast - pad
S430[0..5] = E427[0..5] cast - pad
S431[0..1] = E427[6..7] cast - pad
EXECP priority, buffered queues only {
	START_DEL SD436
	PRI_IN    WPIN423[0]
	<-
	CLK       glob.c32
	START_IN  TS416
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
	PRI_OUT   prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.pv[1]
}
DEL F426 <- SD436 CLK glob.c32 delay 1
DEL F439 <- F426 CLK glob.c32 delay 1
OP DO446 = FMOD7/uart_cpu.command[6..7] == 0	(unsigned, unsigned)
CS440 = F426 AND DO446
OP DO454 = FMOD7/uart_cpu.command[6..7] == 2	(unsigned, unsigned)
CS448 = F426 AND DO454
OP DO462 = FMOD7/uart_cpu.command[6..7] == 1	(unsigned, unsigned)
CS456 = F426 AND DO462
OP DO470 = FMOD7/uart_cpu.command[6..7] == 3	(unsigned, unsigned)
CS464 = F426 AND DO470
E482[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
E486[0..7] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7] cast - pad
S489[0..15] = E486[0..7] cast - pad
EXECP no priority, buffered queues only {
	START_DEL SD494
	<-
	CLK       glob.c32
	START_IN  TS479
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F485 <- SD494 CLK glob.c32 delay 1
E498[0..7] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7] cast - pad
OP E499[0..15] = E498[0..7] << 8	(unsigned, unsigned)
OP E500[0..15] = FMOD7/uart_cpu.memaddr[0..15] | E499[0..15]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD507
	<-
	CLK       glob.c32
	START_IN  F485
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F497 <- SD507 CLK glob.c32 delay 1
DEL FF509 <- FS480 CLK glob.c32 delay 1
WHEN {
	T_START  TS479
	F_START  FS480
	FINISH   F478
	<-
	START    F439
	TEST     E482[0]
	T_FINISH F497
	F_FINISH FF509
}
DEL F513 <- F478 CLK glob.c32 delay 1
DEL FF517 <- FS417 CLK glob.c32 delay 1
WHEN {
	T_START  TS416
	F_START  FS417
	FINISH   F415
	<-
	START    S414
	TEST     E422[0]
	T_FINISH F513
	F_FINISH FF517
}
ILOOP  S414 <- SSD1945 F415
OP E533[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E534[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E536[0] = FMOD7/uart_cpu.command[0..5] == 1	(unsigned, unsigned)
E537[0] = E536[0] AND E533[0] AND E534[0]
DEL F547 <- TS530 CLK glob.c32 delay 1
OP E559[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD571
	<-
	CLK       glob.c32
	START_IN  F547
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F566 <- SD571 CLK glob.c32 delay 1
OP E574[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.i[0..2] != 4	(unsigned, unsigned)
OP E578[0..31] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.data[0..31]	(unsigned, unsigned, unsigned)
S581[0..7] = E578[0..31] cast - pad
DEL F577 <- SD601 CLK glob.c32 delay 1
OP E587[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.i[0..2] + 1	(unsigned, unsigned)
S590[0..2] = E587[0..3] cast - pad
OP E594[0..31] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.data[0..31] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD601
	<-
	CLK       glob.c32
	START_IN  SB573
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB573
	FINISH     F604
	<-
	START      F566
	TEST       E574[0]
	CONTIN     F577
	C          glob.c32
	RESET      null
}
DEL S611 <- F604 CLK glob.c32 delay 1
DEL FF621 <- FS531 CLK glob.c32 delay 1
WHEN {
	T_START  TS530
	F_START  FS531
	FINISH   F529
	<-
	START    S528
	TEST     E537[0]
	T_FINISH S611
	F_FINISH FF621
}
ILOOP  S528 <- SSD1945 F529
OP E638[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E639[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E641[0] = FMOD7/uart_cpu.command[0..5] == 1	(unsigned, unsigned)
E642[0] = E641[0] AND E638[0] AND E639[0]
WHEN {
	T_START  TS635
	F_START  FS636
	FINISH   -
	<-
	START    SDD2725
	TEST     E642[0]
	T_FINISH -
	F_FINISH -
}
OP E674[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E675[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E677[0] = FMOD7/uart_cpu.command[0..5] == 2	(unsigned, unsigned)
DEL F682 <- TS671 CLK glob.c32 delay 1
OP E685[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.i[0..1] != 2	(unsigned, unsigned)
OP E690[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.data[0..15] | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD698
	<-
	CLK       glob.c32
	START_IN  SB684
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F689 <- SD698 CLK glob.c32 delay 1
OP E702[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.i[0..1] + 1	(unsigned, unsigned)
S705[0..1] = E702[0..2] cast - pad
OP E711[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.data[0..15] >r> 8	(unsigned, unsigned)
DEL F710 <- F689 CLK glob.c32 delay 1
WHILE {
	START_B    SB684
	FINISH     F717
	<-
	START      F682
	TEST       E685[0]
	CONTIN     F710
	C          glob.c32
	RESET      null
}
E724[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.data[0..15] cast - pad
DEL F723 <- F717 CLK glob.c32 delay 1
DEL S730 <- F723 CLK glob.c32 delay 1
AND743[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE AND E677[0] AND E674[0] AND E675[0]
WHEN {
	T_START  TS671
	F_START  FS672
	FINISH   F670
	<-
	START    S669
	TEST     AND743[0]
	T_FINISH S730
	F_FINISH FF673
}
DEL FF673 <- FS672 CLK glob.c32 delay 1
ILOOP  S669 <- SSD1945 F670
OP E758[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E759[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E761[0] = FMOD7/uart_cpu.command[0..5] == 2	(unsigned, unsigned)
E762[0] = E761[0] AND E758[0] AND E759[0]
DEL F772 <- TS755 CLK glob.c32 delay 1
OP E784[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD796
	<-
	CLK       glob.c32
	START_IN  F772
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F791 <- SD796 CLK glob.c32 delay 1
OP E799[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.i[0..1] != 2	(unsigned, unsigned)
OP E803[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.data[0..15]	(unsigned, unsigned, unsigned)
S806[0..7] = E803[0..15] cast - pad
DEL F802 <- SD826 CLK glob.c32 delay 1
OP E812[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.i[0..1] + 1	(unsigned, unsigned)
S815[0..1] = E812[0..2] cast - pad
OP E819[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD826
	<-
	CLK       glob.c32
	START_IN  SB798
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB798
	FINISH     F829
	<-
	START      F791
	TEST       E799[0]
	CONTIN     F802
	C          glob.c32
	RESET      null
}
DEL S836 <- F829 CLK glob.c32 delay 1
DEL FF846 <- FS756 CLK glob.c32 delay 1
WHEN {
	T_START  TS755
	F_START  FS756
	FINISH   F754
	<-
	START    S753
	TEST     E762[0]
	T_FINISH S836
	F_FINISH FF846
}
ILOOP  S753 <- SSD1945 F754
OP E857[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E858[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E860[0] = FMOD7/uart_cpu.command[0..5] == 3	(unsigned, unsigned)
DEL F865 <- TS854 CLK glob.c32 delay 1
OP E868[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.i[0..2] != 5	(unsigned, unsigned)
OP E873[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.data[0..39] | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD881
	<-
	CLK       glob.c32
	START_IN  SB867
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F872 <- SD881 CLK glob.c32 delay 1
OP E885[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.i[0..2] + 1	(unsigned, unsigned)
S888[0..2] = E885[0..3] cast - pad
OP E894[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.data[0..39] >r> 8	(unsigned, unsigned)
DEL F893 <- F872 CLK glob.c32 delay 1
WHILE {
	START_B    SB867
	FINISH     F900
	<-
	START      F865
	TEST       E868[0]
	CONTIN     F893
	C          glob.c32
	RESET      null
}
E907[0..34] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.data[0..39] cast - sign_extend
DEL F906 <- F900 CLK glob.c32 delay 1
DEL S913 <- F906 CLK glob.c32 delay 1
AND926[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE AND E860[0] AND E857[0] AND E858[0]
WHEN {
	T_START  TS854
	F_START  FS855
	FINISH   F853
	<-
	START    S852
	TEST     AND926[0]
	T_FINISH S913
	F_FINISH FF856
}
DEL FF856 <- FS855 CLK glob.c32 delay 1
ILOOP  S852 <- SSD1945 F853
OP E941[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E942[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E944[0] = FMOD7/uart_cpu.command[0..5] == 3	(unsigned, unsigned)
E945[0] = E944[0] AND E941[0] AND E942[0]
E956[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.in_r[0..34] cast - sign_extend
DEL F955 <- TS938 CLK glob.c32 delay 1
OP E967[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD979
	<-
	CLK       glob.c32
	START_IN  F955
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F974 <- SD979 CLK glob.c32 delay 1
OP E982[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.i[0..2] != 5	(unsigned, unsigned)
OP E986[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.data[0..39]	(unsigned, unsigned, unsigned)
S989[0..7] = E986[0..39] cast - pad
DEL F985 <- SD1009 CLK glob.c32 delay 1
OP E995[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.i[0..2] + 1	(unsigned, unsigned)
S998[0..2] = E995[0..3] cast - pad
OP E1002[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.data[0..39] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1009
	<-
	CLK       glob.c32
	START_IN  SB981
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB981
	FINISH     F1012
	<-
	START      F974
	TEST       E982[0]
	CONTIN     F985
	C          glob.c32
	RESET      null
}
DEL S1019 <- F1012 CLK glob.c32 delay 1
DEL FF1029 <- FS939 CLK glob.c32 delay 1
WHEN {
	T_START  TS938
	F_START  FS939
	FINISH   F937
	<-
	START    S936
	TEST     E945[0]
	T_FINISH S1019
	F_FINISH FF1029
}
ILOOP  S936 <- SSD1945 F937
OP E1040[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1041[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E1043[0] = FMOD7/uart_cpu.command[0..5] == 4	(unsigned, unsigned)
DEL F1048 <- TS1037 CLK glob.c32 delay 1
OP E1051[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.i[0] != 1	(unsigned, unsigned)
OP E1056[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.data[0..7] | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1064
	<-
	CLK       glob.c32
	START_IN  SB1050
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F1055 <- SD1064 CLK glob.c32 delay 1
OP E1068[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.i[0] + 1	(unsigned, unsigned)
S1071[0] = E1068[0..1] cast - pad
OP E1077[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.data[0..7] >r> 8	(unsigned, unsigned)
DEL F1076 <- F1055 CLK glob.c32 delay 1
WHILE {
	START_B    SB1050
	FINISH     F1083
	<-
	START      F1048
	TEST       E1051[0]
	CONTIN     F1076
	C          glob.c32
	RESET      null
}
E1090[0..5] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.data[0..7] cast - pad
DEL F1089 <- F1083 CLK glob.c32 delay 1
DEL S1096 <- F1089 CLK glob.c32 delay 1
AND1109[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE AND E1043[0] AND E1040[0] AND E1041[0]
WHEN {
	T_START  TS1037
	F_START  FS1038
	FINISH   F1036
	<-
	START    S1035
	TEST     AND1109[0]
	T_FINISH S1096
	F_FINISH FF1039
}
DEL FF1039 <- FS1038 CLK glob.c32 delay 1
ILOOP  S1035 <- SSD1945 F1036
OP E1130[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1131[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E1133[0] = FMOD7/uart_cpu.command[0..5] == 4	(unsigned, unsigned)
E1134[0] = E1133[0] AND E1130[0] AND E1131[0]
E1145[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.in_r[0..5] cast - pad
DEL F1144 <- TS1127 CLK glob.c32 delay 1
OP E1156[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1168
	<-
	CLK       glob.c32
	START_IN  F1144
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F1163 <- SD1168 CLK glob.c32 delay 1
OP E1171[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.i[0] != 1	(unsigned, unsigned)
OP E1175[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.data[0..7]	(unsigned, unsigned, unsigned)
DEL F1174 <- SD1197 CLK glob.c32 delay 1
OP E1183[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.i[0] + 1	(unsigned, unsigned)
S1186[0] = E1183[0..1] cast - pad
OP E1190[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.data[0..7] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1197
	<-
	CLK       glob.c32
	START_IN  SB1170
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1170
	FINISH     F1200
	<-
	START      F1163
	TEST       E1171[0]
	CONTIN     F1174
	C          glob.c32
	RESET      null
}
DEL S1207 <- F1200 CLK glob.c32 delay 1
DEL FF1217 <- FS1128 CLK glob.c32 delay 1
WHEN {
	T_START  TS1127
	F_START  FS1128
	FINISH   F1126
	<-
	START    S1125
	TEST     E1134[0]
	T_FINISH S1207
	F_FINISH FF1217
}
ILOOP  S1125 <- SSD1945 F1126
OP E1225[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1226[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E1228[0] = FMOD7/uart_cpu.command[0..5] == 5	(unsigned, unsigned)
DEL F1233 <- TS1222 CLK glob.c32 delay 1
OP E1236[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.i[0..1] != 2	(unsigned, unsigned)
OP E1241[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.data[0..15] | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1249
	<-
	CLK       glob.c32
	START_IN  SB1235
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F1240 <- SD1249 CLK glob.c32 delay 1
OP E1253[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.i[0..1] + 1	(unsigned, unsigned)
S1256[0..1] = E1253[0..2] cast - pad
OP E1262[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.data[0..15] >r> 8	(unsigned, unsigned)
DEL F1261 <- F1240 CLK glob.c32 delay 1
WHILE {
	START_B    SB1235
	FINISH     F1268
	<-
	START      F1233
	TEST       E1236[0]
	CONTIN     F1261
	C          glob.c32
	RESET      null
}
E1281[0..13] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.data[0..15] cast - sign_extend
DEL F1273 <- F1268 CLK glob.c32 delay 1
WHEN {
	T_START  TS1274
	F_START  FS1275
	FINISH   -
	<-
	START    F1268
	TEST     prog.q.NF
	T_FINISH -
	F_FINISH -
}
DEL S1290 <- F1273 CLK glob.c32 delay 1
AND1303[0] = E1228[0] AND E1225[0] AND E1226[0] AND prog.q.NF AND prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
WHEN {
	T_START  TS1222
	F_START  FS1223
	FINISH   F1221
	<-
	START    S1220
	TEST     AND1303[0]
	T_FINISH S1290
	F_FINISH FF1224
}
DEL FF1224 <- FS1223 CLK glob.c32 delay 1
ILOOP  S1220 <- SSD1945 F1221
OP E1310[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1311[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E1313[0] = FMOD7/uart_cpu.command[0..5] == 5	(unsigned, unsigned)
E1314[0] = E1313[0] AND E1310[0] AND E1311[0]
E1325[0..7] = prog.q.SPC[0..4] cast - pad
DEL F1324 <- TS1307 CLK glob.c32 delay 1
OP E1336[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1348
	<-
	CLK       glob.c32
	START_IN  F1324
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F1343 <- SD1348 CLK glob.c32 delay 1
OP E1351[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.i[0] != 1	(unsigned, unsigned)
OP E1355[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.data[0..7]	(unsigned, unsigned, unsigned)
DEL F1354 <- SD1377 CLK glob.c32 delay 1
OP E1363[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.i[0] + 1	(unsigned, unsigned)
S1366[0] = E1363[0..1] cast - pad
OP E1370[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.data[0..7] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1377
	<-
	CLK       glob.c32
	START_IN  SB1350
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1350
	FINISH     F1380
	<-
	START      F1343
	TEST       E1351[0]
	CONTIN     F1354
	C          glob.c32
	RESET      null
}
DEL S1387 <- F1380 CLK glob.c32 delay 1
DEL FF1397 <- FS1308 CLK glob.c32 delay 1
WHEN {
	T_START  TS1307
	F_START  FS1308
	FINISH   F1306
	<-
	START    S1305
	TEST     E1314[0]
	T_FINISH S1387
	F_FINISH FF1397
}
ILOOP  S1305 <- SSD1945 F1306
OP E1405[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1406[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E1408[0] = FMOD7/uart_cpu.command[0..5] == 6	(unsigned, unsigned)
DEL F1413 <- TS1402 CLK glob.c32 delay 1
OP E1416[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.i[0] != 1	(unsigned, unsigned)
OP E1421[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.data[0..7] | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1428
	<-
	CLK       glob.c32
	START_IN  SB1415
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F1420 <- SD1428 CLK glob.c32 delay 1
OP E1432[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.i[0] + 1	(unsigned, unsigned)
S1435[0] = E1432[0..1] cast - pad
OP E1441[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.data[0..7] >r> 8	(unsigned, unsigned)
DEL F1440 <- F1420 CLK glob.c32 delay 1
WHILE {
	START_B    SB1415
	FINISH     F1447
	<-
	START      F1413
	TEST       E1416[0]
	CONTIN     F1440
	C          glob.c32
	RESET      null
}
E1454[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.data[0..7] cast - pad
DEL F1453 <- F1447 CLK glob.c32 delay 1
DEL S1460 <- F1453 CLK glob.c32 delay 1
AND1473[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE AND E1408[0] AND E1405[0] AND E1406[0]
WHEN {
	T_START  TS1402
	F_START  FS1403
	FINISH   F1401
	<-
	START    S1400
	TEST     AND1473[0]
	T_FINISH S1460
	F_FINISH FF1404
}
DEL FF1404 <- FS1403 CLK glob.c32 delay 1
OP E1474[0] = prog.q.SPC[0..4] >= FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.ilevel[0..3]	(unsigned, unsigned)
DEL F1483 <- TS1477 CLK glob.c32 delay 1
DEL FB1490 <- SB1489 CLK glob.c32 delay 1
WHILE {
	START_B    SB1489
	FINISH     F1493
	<-
	START      F1483
	TEST       E1474[0]
	CONTIN     FB1490
	C          glob.c32
	RESET      null
}
AND1501[0] = E1474[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.sigint_2.q.NF
WHEN {
	T_START  TS1477
	F_START  FS1478
	FINISH   F1476
	<-
	START    S1475
	TEST     AND1501[0]
	T_FINISH F1493
	F_FINISH FF1479
}
DEL FF1479 <- FS1478 CLK glob.c32 delay 1
ILOOP  S1475 <- SSD1945 F1476
ILOOP  S1400 <- SSD1945 F1401
OP E1512[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1513[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E1515[0] = FMOD7/uart_cpu.command[0..5] == 6	(unsigned, unsigned)
E1516[0] = E1515[0] AND E1512[0] AND E1513[0]
E1527[0..15] = prog.q[0..13] cast - sign_extend
EXECP no priority, buffered queues only {
	START_DEL SD1534
	<-
	CLK       glob.c32
	START_IN  TS1520
	BQAV      MRAV1505
}
DEL F1526 <- SD1534 CLK glob.c32 delay 1
AND1549 = prog.qo.NF AND MRAV1505
EXECP no priority, buffered queues only {
	START_DEL SD1550
	<-
	CLK       glob.c32
	START_IN  TS1537
	BQAV      AND1549
}
DEL F1543 <- SD1550 CLK glob.c32 delay 1
DEL FF1551 <- FS1538 CLK glob.c32 delay 1
WHEN {
	T_START  TS1537
	F_START  FS1538
	FINISH   F1536
	<-
	START    TS1520
	TEST     prog.qo.NF
	T_FINISH F1543
	F_FINISH FF1551
}
WAIT {
    in:
        glob.c32
        null
        F1526
        F1536
    out:
        F1554
}
DEL FF1555 <- FS1521 CLK glob.c32 delay 1
WHEN {
	T_START  TS1520
	F_START  FS1521
	FINISH   F1519
	<-
	START    TS1509
	TEST     MRAV1505
	T_FINISH F1554
	F_FINISH FF1555
}
DEL F1559 <- TS1509 CLK glob.c32 delay 1
OP E1563[0] = ~MRAV1505	(unsigned)
WAIT {
    in:
        glob.c32
        null
        F1519
        F1559
    out:
        F1567
}
EXECP no priority, buffered queues only {
	START_DEL SD1575
	<-
	CLK       glob.c32
	START_IN  F1567
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F1570 <- SD1575 CLK glob.c32 delay 1
OP E1578[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.i[0..1] != 2	(unsigned, unsigned)
OP E1582[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.data[0..15]	(unsigned, unsigned, unsigned)
S1585[0..7] = E1582[0..15] cast - pad
DEL F1581 <- SD1605 CLK glob.c32 delay 1
OP E1591[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.i[0..1] + 1	(unsigned, unsigned)
S1594[0..1] = E1591[0..2] cast - pad
OP E1598[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1605
	<-
	CLK       glob.c32
	START_IN  SB1577
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1577
	FINISH     F1608
	<-
	START      F1570
	TEST       E1578[0]
	CONTIN     F1581
	C          glob.c32
	RESET      null
}
DEL S1615 <- F1608 CLK glob.c32 delay 1
DEL FF1625 <- FS1510 CLK glob.c32 delay 1
WHEN {
	T_START  TS1509
	F_START  FS1510
	FINISH   F1508
	<-
	START    S1507
	TEST     E1516[0]
	T_FINISH S1615
	F_FINISH FF1625
}
ILOOP  S1507 <- SSD1945 F1508
OP E1639[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1640[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E1642[0] = FMOD7/uart_cpu.command[0..5] == 7	(unsigned, unsigned)
E1643[0] = E1642[0] AND E1639[0] AND E1640[0]
E1654[0..7] = prog.q.CNT[0..4] cast - pad
DEL F1653 <- TS1636 CLK glob.c32 delay 1
OP E1665[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1677
	<-
	CLK       glob.c32
	START_IN  F1653
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F1672 <- SD1677 CLK glob.c32 delay 1
OP E1680[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.i[0] != 1	(unsigned, unsigned)
OP E1684[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.data[0..7]	(unsigned, unsigned, unsigned)
DEL F1683 <- SD1706 CLK glob.c32 delay 1
OP E1692[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.i[0] + 1	(unsigned, unsigned)
S1695[0] = E1692[0..1] cast - pad
OP E1699[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.data[0..7] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1706
	<-
	CLK       glob.c32
	START_IN  SB1679
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1679
	FINISH     F1709
	<-
	START      F1672
	TEST       E1680[0]
	CONTIN     F1683
	C          glob.c32
	RESET      null
}
DEL S1716 <- F1709 CLK glob.c32 delay 1
DEL FF1726 <- FS1637 CLK glob.c32 delay 1
WHEN {
	T_START  TS1636
	F_START  FS1637
	FINISH   F1635
	<-
	START    S1634
	TEST     E1643[0]
	T_FINISH S1716
	F_FINISH FF1726
}
ILOOP  S1634 <- SSD1945 F1635
OP E1734[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1735[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E1737[0] = FMOD7/uart_cpu.command[0..5] == 7	(unsigned, unsigned)
DEL F1742 <- TS1731 CLK glob.c32 delay 1
OP E1745[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.i[0] != 1	(unsigned, unsigned)
OP E1750[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.data[0..7] | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1758
	<-
	CLK       glob.c32
	START_IN  SB1744
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F1749 <- SD1758 CLK glob.c32 delay 1
OP E1762[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.i[0] + 1	(unsigned, unsigned)
S1765[0] = E1762[0..1] cast - pad
OP E1771[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.data[0..7] >r> 8	(unsigned, unsigned)
DEL F1770 <- F1749 CLK glob.c32 delay 1
WHILE {
	START_B    SB1744
	FINISH     F1777
	<-
	START      F1742
	TEST       E1745[0]
	CONTIN     F1770
	C          glob.c32
	RESET      null
}
E1784[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.data[0..7] cast - pad
DEL F1783 <- F1777 CLK glob.c32 delay 1
DEL S1790 <- F1783 CLK glob.c32 delay 1
AND1803[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE AND E1737[0] AND E1734[0] AND E1735[0]
WHEN {
	T_START  TS1731
	F_START  FS1732
	FINISH   F1730
	<-
	START    S1729
	TEST     AND1803[0]
	T_FINISH S1790
	F_FINISH FF1733
}
DEL FF1733 <- FS1732 CLK glob.c32 delay 1
OP E1804[0] = prog.q.CNT[0..4] >= FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.ilevel[0..3]	(unsigned, unsigned)
DEL F1813 <- TS1807 CLK glob.c32 delay 1
DEL FB1820 <- SB1819 CLK glob.c32 delay 1
WHILE {
	START_B    SB1819
	FINISH     F1823
	<-
	START      F1813
	TEST       E1804[0]
	CONTIN     FB1820
	C          glob.c32
	RESET      null
}
AND1831[0] = E1804[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.sigint_3.q.NF
WHEN {
	T_START  TS1807
	F_START  FS1808
	FINISH   F1806
	<-
	START    S1805
	TEST     AND1831[0]
	T_FINISH F1823
	F_FINISH FF1809
}
DEL FF1809 <- FS1808 CLK glob.c32 delay 1
ILOOP  S1805 <- SSD1945 F1806
ILOOP  S1729 <- SSD1945 F1730
OP E1848[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1849[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E1851[0] = FMOD7/uart_cpu.command[0..5] == 8	(unsigned, unsigned)
E1852[0] = E1851[0] AND E1848[0] AND E1849[0]
E1863[0..15] = prog.q[0..13] cast - sign_extend
EXECP no priority, buffered queues only {
	START_DEL SD1870
	<-
	CLK       glob.c32
	START_IN  TS1856
	BQAV      MRAV1841
}
DEL F1862 <- SD1870 CLK glob.c32 delay 1
DEL FF1873 <- FS1857 CLK glob.c32 delay 1
WHEN {
	T_START  TS1856
	F_START  FS1857
	FINISH   F1855
	<-
	START    TS1845
	TEST     MRAV1841
	T_FINISH F1862
	F_FINISH FF1873
}
DEL F1877 <- TS1845 CLK glob.c32 delay 1
OP E1881[0] = ~MRAV1841	(unsigned)
WAIT {
    in:
        glob.c32
        null
        F1855
        F1877
    out:
        F1885
}
EXECP no priority, buffered queues only {
	START_DEL SD1893
	<-
	CLK       glob.c32
	START_IN  F1885
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F1888 <- SD1893 CLK glob.c32 delay 1
OP E1896[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.i[0..1] != 2	(unsigned, unsigned)
OP E1900[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.data[0..15]	(unsigned, unsigned, unsigned)
S1903[0..7] = E1900[0..15] cast - pad
DEL F1899 <- SD1923 CLK glob.c32 delay 1
OP E1909[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.i[0..1] + 1	(unsigned, unsigned)
S1912[0..1] = E1909[0..2] cast - pad
OP E1916[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1923
	<-
	CLK       glob.c32
	START_IN  SB1895
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1895
	FINISH     F1926
	<-
	START      F1888
	TEST       E1896[0]
	CONTIN     F1899
	C          glob.c32
	RESET      null
}
DEL S1933 <- F1926 CLK glob.c32 delay 1
DEL FF1943 <- FS1846 CLK glob.c32 delay 1
WHEN {
	T_START  TS1845
	F_START  FS1846
	FINISH   F1844
	<-
	START    S1843
	TEST     E1852[0]
	T_FINISH S1933
	F_FINISH FF1943
}
DEL SSD1945 <- glob.c32.start CLK glob.c32 delay 1
ILOOP  S1843 <- SSD1945 F1844
OP E1954[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E1955[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
OP E1957[0] = FMOD7/uart_cpu.command[0..5] == 8	(unsigned, unsigned)
DEL F1962 <- TS1951 CLK glob.c32 delay 1
OP E1965[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.i[0..1] != 2	(unsigned, unsigned)
OP E1970[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.data[0..15] | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1978
	<-
	CLK       glob.c32
	START_IN  SB1964
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
}
DEL F1969 <- SD1978 CLK glob.c32 delay 1
OP E1982[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.i[0..1] + 1	(unsigned, unsigned)
S1985[0..1] = E1982[0..2] cast - pad
OP E1991[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.data[0..15] >r> 8	(unsigned, unsigned)
DEL F1990 <- F1969 CLK glob.c32 delay 1
WHILE {
	START_B    SB1964
	FINISH     F1997
	<-
	START      F1962
	TEST       E1965[0]
	CONTIN     F1990
	C          glob.c32
	RESET      null
}
E2004[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.data[0..15] cast - pad
DEL F2003 <- F1997 CLK glob.c32 delay 1
DEL S2010 <- F2003 CLK glob.c32 delay 1
AND2023[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE AND E1957[0] AND E1954[0] AND E1955[0]
WHEN {
	T_START  TS1951
	F_START  FS1952
	FINISH   F1950
	<-
	START    S1949
	TEST     AND2023[0]
	T_FINISH S2010
	F_FINISH FF1953
}
DEL FF1953 <- FS1952 CLK glob.c32 delay 1
ILOOP  S1949 <- SSD1945 F1950
E2033[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.mdata[0..15] cast - pad
S2035[0..7] = FMOD7/uart_cpu.memaddr[0..15] cast - pad
MADDR2034[0..7] = S2035[0..7] cast - pad
MDATA2036[0..15] = E2033[0..15] cast - pad
WHEN {
	T_START  TS2027
	F_START  FS2028
	FINISH   -
	<-
	START    SDD2712
	TEST     F2003
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.data_in_1[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.data[0..15]
OP E2049[0] = ~prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]	(unsigned)
E2050[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0] OR prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
OP E2052[0] = FMOD7/uart_cpu.command[0..5] == 9	(unsigned, unsigned)
E2053[0] = E2052[0] AND E2049[0] AND E2050[0]
DEL S2056 <- TS2046 CLK glob.c32 delay 1
OP E2060[0] = ~F2166	(unsigned)
DEL FB2061 <- SB2059 CLK glob.c32 delay 1
WHILE {
	START_B    SB2059
	FINISH     F2064
	<-
	START      S2056
	TEST       E2060[0]
	CONTIN     FB2061
	C          glob.c32
	RESET      null
}
E2078[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.data_in_1[0..15] cast - pad
DEL F2077 <- F2064 CLK glob.c32 delay 1
OP E2089[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2101
	<-
	CLK       glob.c32
	START_IN  F2077
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
DEL F2096 <- SD2101 CLK glob.c32 delay 1
OP E2104[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.i[0..1] != 2	(unsigned, unsigned)
OP E2108[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.data[0..15]	(unsigned, unsigned, unsigned)
S2111[0..7] = E2108[0..15] cast - pad
DEL F2107 <- SD2131 CLK glob.c32 delay 1
OP E2117[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.i[0..1] + 1	(unsigned, unsigned)
S2120[0..1] = E2117[0..2] cast - pad
OP E2124[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2131
	<-
	CLK       glob.c32
	START_IN  SB2103
	BQAV      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB2103
	FINISH     F2134
	<-
	START      F2096
	TEST       E2104[0]
	CONTIN     F2107
	C          glob.c32
	RESET      null
}
DEL S2141 <- F2134 CLK glob.c32 delay 1
DEL FF2151 <- FS2047 CLK glob.c32 delay 1
WHEN {
	T_START  TS2046
	F_START  FS2047
	FINISH   F2045
	<-
	START    S2044
	TEST     E2053[0]
	T_FINISH S2141
	F_FINISH FF2151
}
S2162[0..7] = FMOD7/uart_cpu.memaddr[0..15] cast - pad
MADDR2161[0..7] = S2162[0..7] cast - pad
DEL S2160 <- TS2155 CLK glob.c32 delay 1
DEL F2166 <- S2160 CLK glob.c32 delay 1
DEL S2172 <- F2166 CLK glob.c32 delay 1
DEL FF2175 <- FS2156 CLK glob.c32 delay 1
WHEN {
	T_START  TS2155
	F_START  FS2156
	FINISH   F2154
	<-
	START    S2153
	TEST     TS2046
	T_FINISH S2172
	F_FINISH FF2175
}
ILOOP  S2044 <- SSD1945 F2045
ILOOP  S2153 <- SSD1945 F2154
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.in0_1[0..4] = prog.receive_event_0.sigint_0.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.in1_1[0..4] = prog.receive_event_1.sigint_1.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.select1[0]
OP E2187[0] = ~prog.receive_event_1.sigint_1.q.NE	(unsigned)
OP E2188[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.select1_1[0]	(unsigned)
E2189[0] = E2187[0] OR E2188[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.par_56.assign_port_0.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.in0_1[0..4]
S2196[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.par_56.assign_port_0.in_1[0..4] cast - pad
AND2209[0] = E2189[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.NF AND prog.receive_event_0.sigint_0.q.NE
WHEN {
	T_START  TS2181
	F_START  FS2182
	FINISH   -
	<-
	START    SDD2718
	TEST     AND2209[0]
	T_FINISH -
	F_FINISH -
}
OP E2218[0] = ~prog.receive_event_0.sigint_0.q.NE	(unsigned)
E2219[0] = E2218[0] OR FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.select1_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.par_57.assign_port_1.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.in1_1[0..4]
S2226[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.mux2_0.par_57.assign_port_1.in_1[0..4] cast - pad
OR2235[0] = TS2181 OR TS2213
AND2239[0] = E2219[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.NF AND prog.receive_event_1.sigint_1.q.NE
WHEN {
	T_START  TS2213
	F_START  FS2214
	FINISH   -
	<-
	START    SDD2719
	TEST     AND2239[0]
	T_FINISH -
	F_FINISH -
}
OP E2249[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.select1[0]	(unsigned)
WHEN {
	T_START  TS2243
	F_START  FS2244
	FINISH   -
	<-
	START    SDD2713
	TEST     OR2235[0]
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.in0_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1[0..6]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.in1_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2[0..6]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.select1_1[0]
OP E2263[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.NE	(unsigned)
OP E2264[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.select1_1[0]	(unsigned)
E2265[0] = E2263[0] OR E2264[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.par_58.assign_port_2.in_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.in0_1[0..6]
AND2284[0] = E2265[0] AND prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.NE
WHEN {
	T_START  TS2257
	F_START  FS2258
	FINISH   -
	<-
	START    SDD2720
	TEST     AND2284[0]
	T_FINISH -
	F_FINISH -
}
OP E2293[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.NE	(unsigned)
E2294[0] = E2293[0] OR FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.select1_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.par_59.assign_port_3.in_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.in1_1[0..6]
OR2309[0] = TS2257 OR TS2288
AND2313[0] = E2294[0] AND prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.NE
WHEN {
	T_START  TS2288
	F_START  FS2289
	FINISH   -
	<-
	START    SDD2721
	TEST     AND2313[0]
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.in0_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.sigint_2.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.in1_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.sigint_3.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.select1[0]
OP E2323[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.sigint_3.q.NE	(unsigned)
OP E2324[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.select1_1[0]	(unsigned)
E2325[0] = E2323[0] OR E2324[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.par_60.assign_port_4.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.in0_1[0..4]
S2332[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.par_60.assign_port_4.in_1[0..4] cast - pad
AND2345[0] = E2325[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.sigint_2.q.NE
WHEN {
	T_START  TS2317
	F_START  FS2318
	FINISH   -
	<-
	START    SDD2722
	TEST     AND2345[0]
	T_FINISH -
	F_FINISH -
}
OP E2354[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.sigint_2.q.NE	(unsigned)
E2355[0] = E2354[0] OR FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.select1_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.par_61.assign_port_5.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.in1_1[0..4]
S2362[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.mux2_2.par_61.assign_port_5.in_1[0..4] cast - pad
OR2371[0] = TS2317 OR TS2349
AND2375[0] = E2355[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.sigint_3.q.NE
WHEN {
	T_START  TS2349
	F_START  FS2350
	FINISH   -
	<-
	START    SDD2723
	TEST     AND2375[0]
	T_FINISH -
	F_FINISH -
}
OP E2385[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.select1[0]	(unsigned)
WHEN {
	T_START  TS2379
	F_START  FS2380
	FINISH   -
	<-
	START    SDD2714
	TEST     OR2371[0]
	T_FINISH -
	F_FINISH -
}
OP E2399[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_235.s1[0]	(unsigned)
WHEN {
	T_START  TS2393
	F_START  FS2394
	FINISH   -
	<-
	START    SDD2715
	TEST     OR2309[0]
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_235.s1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.select1_2[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_235.s1[0]
OP E2422[0..7] = 128 | prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq[0..6]	(unsigned, unsigned)
AND2428 = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF AND prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.NE
EXECP priority, buffered queues only {
	START_DEL SD2429
	PRI_IN    WPIN2418[0]
	<-
	CLK       glob.c32
	START_IN  TS2410
	BQAV      AND2428
	PRI_OUT   prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.pv[2]
}
DEL F2421 <- SD2429 CLK glob.c32 delay 1
AND2433[0] = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0] AND prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.NE AND prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
WHEN {
	T_START  TS2410
	F_START  FS2411
	FINISH   F2409
	<-
	START    S2408
	TEST     AND2433[0]
	T_FINISH F2421
	F_FINISH FF2412
}
DEL FF2412 <- FS2411 CLK glob.c32 delay 1
ILOOP  S2408 <- SSD1945 F2409
WHEN {
	T_START  TS2440
	F_START  FS2441
	FINISH   -
	<-
	START    SDD2716
	TEST     prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.inforead[0]
	T_FINISH -
	F_FINISH -
}
DEL F2457 <- TS2452 CLK glob.c32 delay 1
OR2464[0] = TS2440 OR F2457
DEL S2462 <- F2457 CLK glob.c32 delay 1
DEL FF2466 <- FS2453 CLK glob.c32 delay 1
WHEN {
	T_START  TS2452
	F_START  FS2453
	FINISH   F2451
	<-
	START    S2450
	TEST     prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.inforeset[0]
	T_FINISH S2462
	F_FINISH FF2466
}
ILOOP  S2450 <- SSD1945 F2451
MADDR2478[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7] cast - pad
OP E2485[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7] != 255	(unsigned, unsigned)
OP E2489[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7] + 1	(unsigned, unsigned)
S2492[0..7] = E2489[0..8] cast - pad
WHEN {
	T_START  TS2482
	F_START  FS2483
	FINISH   -
	<-
	START    TS2471
	TEST     E2485[0]
	T_FINISH -
	F_FINISH -
}
WHEN {
	T_START  TS2471
	F_START  FS2472
	FINISH   -
	<-
	START    SDD2724
	TEST     OR2464[0]
	T_FINISH -
	F_FINISH -
}
NCF "NET PORT_P89 IOSTANDARD=LVCMOS33;" port PORT_P89
NCF "NET %n TNM=C32;" port null
NCF "TIMESPEC TS_C32=PERIOD C32 31.25;" port null
NCF "NET PORT_P88 IOSTANDARD=LVCMOS33;" port PORT_P88
NCF "NET PORT_P90 IOSTANDARD=LVCMOS33;" port PORT_P90
OR2500 = TS635 OR F723 OR F1273 OR F604 OR F1453 OR F1783 OR F1926 OR F2134 OR F829 OR F1012 OR F1709 OR F2003 OR F1089 OR F1200 OR F1380 OR F1608 OR F906
REG
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.idle[0]
	<-
	CLK  glob.c32
	D    GND
	CE   F478
	R    OR2500
    {0x1}
REG
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.inforeset[0]
	<-
	CLK  glob.c32
	D    TS635
	CE   SDD2708
	R    GND
    {0x0}
REG
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.inforead[0]
	<-
	CLK  glob.c32
	D    F604
	CE   SDD2707
	R    GND
    {0x0}
prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.D[0..7] = E269[0..7]
QUEUEBUFFER  depth 16 {
	OUT      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx[0..7]
	NE       prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NE
	NF       prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.NF
	<-
	CLK      glob.c32
	DATA     prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.D[0..7]
	PUSH     TS225
	POP      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.POP
	RESET    GND
}
prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rx.POP = SD436 OR SD494 OR SD507 OR SD698 OR SD881 OR SD1064 OR SD1249 OR SD1428 OR SD1758 OR SD1978
OR2524 = SD2131 OR SD1348 OR SD601 OR SD1605 OR SD571 OR SD1923 OR SD1893 OR SD1706 OR SD2429 OR SD2101 OR SD1677 OR SD1168 OR SD826 OR SD1009 OR SD979 OR SD1575 OR SD796 OR SD1197 OR SD1377
SELECT {
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.D[0..7]
	<-
	SEL  SD571
	IN   4
	SEL  SD601
	IN   S581[0..7]
	SEL  SD796
	IN   2
	SEL  SD826
	IN   S806[0..7]
	SEL  SD979
	IN   5
	SEL  SD1009
	IN   S989[0..7]
	SEL  SD1168
	IN   1
	SEL  SD1197
	IN   E1175[0..7]
	SEL  SD1348
	IN   1
	SEL  SD1377
	IN   E1355[0..7]
	SEL  SD1575
	IN   2
	SEL  SD1605
	IN   S1585[0..7]
	SEL  SD1677
	IN   1
	SEL  SD1706
	IN   E1684[0..7]
	SEL  SD1893
	IN   2
	SEL  SD1923
	IN   S1903[0..7]
	SEL  SD2101
	IN   2
	SEL  SD2131
	IN   S2111[0..7]
	SEL  SD2429
	IN   E2422[0..7]
    unselected out 0x0
}
QUEUEBUFFER  depth 16 {
	OUT      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx[0..7]
	NE       prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NE
	NF       prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.NF
	<-
	CLK      glob.c32
	DATA     prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.tx.D[0..7]
	PUSH     OR2524
	POP      SD341
	RESET    GND
}
OR2526 = F829 OR F906 OR F1709 OR F2003 OR F1380 OR F604 OR F1453 OR F1783 OR F1012 OR F1273 OR F1608 OR F1089 OR F723 OR F1200 OR F1926 OR F2134 OR TS635
OR2527 = prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.pv[1]
DFF FDRSE {
	OUT      PRILOCK2525
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        OR2526
	S        OR2527
init = R
}
PRI_IN2528[0] = PRILOCK2525
prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.pv[0] = PRI_OUT2529[0]
PRI_IN2528[1] = WPIN423[0]
prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.pv[1] = PRI_OUT2529[1]
PRI_IN2528[2] = WPIN2418[0]
prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.pv[2] = PRI_OUT2529[2]
PRIORITY {
	OUT   PRI_OUT2529[0,1,2]
	<-
	IN    PRI_IN2528[0,1,2]
}
OR2530 = F1012 OR F1709 OR F1200 OR F1380 OR F1608 OR F604 OR F1926 OR F2134 OR F829
REG
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rval[0]
	<-
	CLK  glob.c32
	D    VCC
	CE   CS440
	R    OR2530
    {0x0}
OR2532 = TS635 OR F723 OR F1273 OR F2003 OR F1089 OR F1453 OR F1783 OR F906
REG
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wstat[0]
	<-
	CLK  glob.c32
	D    VCC
	CE   CS448
	R    OR2532
    {0x0}
OR2534 = F1012 OR F1709 OR F1200 OR F1380 OR F1608 OR F604 OR F1926 OR F2134 OR F829
REG
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.rmem[0]
	<-
	CLK  glob.c32
	D    VCC
	CE   CS456
	R    OR2534
    {0x0}
OR2536 = TS635 OR F723 OR F1273 OR F2003 OR F1089 OR F1453 OR F1783 OR F906
REG
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.wmem[0]
	<-
	CLK  glob.c32
	D    VCC
	CE   CS464
	R    OR2536
    {0x0}
OR2540 = TS2288 OR TS2257
SELECT {
	OUT  prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.D[0..6]
	<-
	SEL  TS2257
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.par_58.assign_port_2.in_1[0..6]
	SEL  TS2288
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.mux2_1.par_59.assign_port_3.in_1[0..6]
    unselected out 0x0
}
QUEUEBUFFER  depth 16 {
	OUT      prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq[0..6]
	NE       prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.NE
	NF       prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.NF
	<-
	CLK      glob.c32
	DATA     prog.FMOD1/papilio_one_500.FMOD7/uart_cpu.iq.D[0..6]
	PUSH     OR2540
	POP      SD2429
	RESET    GND
}
prog.r.D[0..15] = E724[0..15]
REG
	OUT  prog.r[0..15]
	<-
	CLK  glob.c32
	D    prog.r.D[0..15]
	CE   F717
	R    GND
    {0x0137}
prog.rr.D[0..34] = E907[0..34]
REG
	OUT  prog.rr[0..34]
	<-
	CLK  glob.c32
	D    prog.rr.D[0..34]
	CE   F900
	R    GND
    {0x0}
prog.rrr.D[0..5] = E1090[0..5]
REG
	OUT  prog.rrr[0..5]
	<-
	CLK  glob.c32
	D    prog.rrr.D[0..5]
	CE   F1083
	R    GND
    {0x0}
REG
	OUT  prog.rrra[0]
	<-
	CLK  glob.c32
	D    F1089
	CE   SDD2709
	R    GND
    {0x0}
QUEUEBUFFER  depth 2 {
	OUT      prog.receive_event_0.sigint_0.q[0..4]
	NE       prog.receive_event_0.sigint_0.q.NE
	NF       prog.receive_event_0.sigint_0.q.NF
	<-
	CLK      glob.c32
	DATA     1
	PUSH     TS5
	POP      TS2181
	RESET    GND
}
prog.q.D[0..13] = E1281[0..13]
QUEUEBUFFER  depth 16 {
	OUT      prog.q[0..13]
	NE       prog.q.NE
	NF       prog.q.NF
	CNT      prog.q.CNT[0..4]
	SPC      prog.q.SPC[0..4]
	<-
	CLK      glob.c32
	DATA     prog.q.D[0..13]
	PUSH     TS1274
	POP      prog.q.POP
	RESET    GND
}
OR2547 = SD1534 OR SD1550
DIVERGE {
    in:
        glob.c32
        GND
        prog.q.NE
        OR2547
        SD1870
    out:
        prog.q.POP
        MRAV1505
        MRAV1841
}
QUEUEBUFFER  depth 16 {
	OUT      prog.qo[0..13]
	NE       prog.qo.NE
	NF       prog.qo.NF
	<-
	CLK      glob.c32
	DATA     prog.q[0..13]
	PUSH     SD1550
	POP      SD70
	RESET    GND
}
REG
	OUT  prog.acc[0]
	<-
	CLK  glob.c32
	D    F1608
	CE   SDD2710
	R    GND
    {0x0}
REG
	OUT  prog.count[0..4]
	<-
	CLK  glob.c32
	D    prog.q.CNT[0..4]
	CE   SDD2711
	R    GND
    {0x0}
QUEUEBUFFER  depth 2 {
	OUT      prog.receive_event_1.sigint_1.q[0..4]
	NE       prog.receive_event_1.sigint_1.q.NE
	NF       prog.receive_event_1.sigint_1.q.NF
	<-
	CLK      glob.c32
	DATA     4
	PUSH     TS34
	POP      TS2213
	RESET    GND
}
SELECT {
	OUT  S2553[0..7]
	<-
	SEL  TS2155
	IN   MADDR2161[0..7]
	SEL  TS2027
	IN   MADDR2034[0..7]
}
S2556[0..15] = MDATA2036[0..15]
RRAM - 1 ports {
	OUT0	prog.m_0[0..15]
	<-
	CLK0	glob.c32
	ADDR0	S2553[0..7]
	DATA0	S2556[0..15]
	RE0	TS2155
	WE0	TS2027
    initialised
}
IBUF  INPUT2557[0] <- PORT_P88 loc=P88 id b291 IBUF
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l.D[0..11] = S91[0..11]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..11]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l.D[0..11]
	CE   SDD2706
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.prev[0]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0]
	CE   SDD2705
	R    GND
    {0x1}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data.D[0..7] = S190[0..7]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data.D[0..7]
	CE   F177
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount.D[0..3] = S197[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount.D[0..3]
	CE   F177
	R    TS133
    {0x8}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.ferr[0]
	<-
	CLK  glob.c32
	D    E220[0]
	CE   F213
	R    TS133
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.oflo[0]
	<-
	CLK  glob.c32
	D    VCC
	CE   FS226
	R    TS133
    {0x0}
OR2568 = F161 OR F148 OR TS133
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count.D[0..3] = S110[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count.D[0..3]
	CE   TS100
	R    OR2568
    {0x9}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]
	<-
	CLK  glob.c32
	D    INPUT2557[0]
	CE   TS100
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16_prev[0]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]
	CE   TS100
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.OUTPUT0[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.tx_out_1[0]
OBUF PORT_P90 <- OUTPUTBIT2572[0] loc=P90 id b328 OBUF
OUTPUTBIT2572[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.OUTPUT0[0] cast - pad
OR2573 = F387 OR F401
OR2576 = F351 OR SD341
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.txd_.D[0]
	<-
	SEL  F351
	IN   E358[0]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.txd_[0]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.txd_.D[0]
	CE   OR2576
	R    OR2573
    {0x1}
OR2577 = FS298 OR SD341
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.txbusy_[0]
	<-
	CLK  glob.c32
	D    GND
	CE   GND
	R    OR2577
    {0x1}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.bitcount.D[0..3] = S373[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.bitcount[0..3]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.bitcount.D[0..3]
	CE   F363
	R    SD341
    {0x8}
OR2582 = F357 OR SD341
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.data.D[0..7]
	<-
	SEL  F357
	IN   E364[0..7]
	SEL  SD341
	IN   E283[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_174.uart_tx_0.data.D[0..7]
	CE   OR2582
	R    GND
    {0x0}
FMOD7/uart_cpu.command.RES[0..5] = GND expand
FMOD7/uart_cpu.command.RES[6..7] = GND expand
FMOD7/uart_cpu.command.D[0..5] = S430[0..5]
FMOD7/uart_cpu.command.D[6..7] = S431[0..1]
FMOD7/uart_cpu.command.CE[0..5] = SD436 expand
FMOD7/uart_cpu.command.CE[6..7] = SD436 expand
REG
	OUT  FMOD7/uart_cpu.command[0..5,6..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.command.D[0..5,6..7]
	CE   FMOD7/uart_cpu.command.CE[0..5,6..7]
	R    FMOD7/uart_cpu.command.RES[0..5,6..7]
    {0x0}
OR2586 = SD507 OR SD494
SELECT {
	OUT  FMOD7/uart_cpu.memaddr.D[0..15]
	<-
	SEL  SD507
	IN   E500[0..15]
	SEL  SD494
	IN   S489[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.memaddr[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.memaddr.D[0..15]
	CE   OR2586
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.in_r[0..31]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.im_0[0..31]
	CE   VCC
	R    GND
    {0x0}
OR2590 = TS530 OR SD601
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.data.D[0..31]
	<-
	SEL  TS530
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.in_r[0..31]
	SEL  SD601
	IN   E594[0..31]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.data[0..31]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.data.D[0..31]
	CE   OR2590
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.empty[0]
	<-
	CLK  glob.c32
	D    E559[0]
	CE   TS530
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.i.D[0..2] = S590[0..2]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.i[0..2]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_77.read_value_gen_0.cpu_read_0.i.D[0..2]
	CE   SD601
	R    TS530
    {0x0}
OR2597 = F689 OR SD698
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.data.D[0..15]
	<-
	SEL  SD698
	IN   E690[0..15]
	SEL  F689
	IN   E711[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.data[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.data.D[0..15]
	CE   OR2597
	R    TS671
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.i.D[0..1] = S705[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.i[0..1]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_79.write_static_gen_1.if_189.cpu_write_1.i.D[0..1]
	CE   SB684
	R    TS671
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.in_r.D[0..15] = prog.read_1.case_15.read_value_1.in_1[0..15]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.in_r[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.in_r.D[0..15]
	CE   VCC
	R    GND
    {0x0}
OR2603 = SD826 OR TS755
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.data.D[0..15]
	<-
	SEL  TS755
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.in_r[0..15]
	SEL  SD826
	IN   E819[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.data[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.data.D[0..15]
	CE   OR2603
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.empty[0]
	<-
	CLK  glob.c32
	D    E784[0]
	CE   TS755
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.i.D[0..1] = S815[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.i[0..1]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_80.read_value_gen_1.cpu_read_1.i.D[0..1]
	CE   SD826
	R    TS755
    {0x0}
OR2610 = SD881 OR F872
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.data.D[0..39]
	<-
	SEL  SD881
	IN   E873[0..39]
	SEL  F872
	IN   E894[0..39]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.data[0..39]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.data.D[0..39]
	CE   OR2610
	R    TS854
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.i.D[0..2] = S888[0..2]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.i[0..2]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_81.write_static_gen_2.if_195.cpu_write_2.i.D[0..2]
	CE   SB867
	R    TS854
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.in_r.D[0..34] = prog.read_2.case_18.read_value_2.in_1[0..34]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.in_r[0..34]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.in_r.D[0..34]
	CE   VCC
	R    GND
    {0x0}
OR2616 = SD1009 OR TS938
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.data.D[0..39]
	<-
	SEL  SD1009
	IN   E1002[0..39]
	SEL  TS938
	IN   E956[0..39]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.data[0..39]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.data.D[0..39]
	CE   OR2616
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.empty[0]
	<-
	CLK  glob.c32
	D    E967[0]
	CE   TS938
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.i.D[0..2] = S998[0..2]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.i[0..2]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_82.read_value_gen_2.cpu_read_2.i.D[0..2]
	CE   SD1009
	R    TS938
    {0x0}
OR2623 = F1055 OR SD1064
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.data.D[0..7]
	<-
	SEL  F1055
	IN   E1077[0..7]
	SEL  SD1064
	IN   E1056[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.data.D[0..7]
	CE   OR2623
	R    TS1037
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_83.write_static_gen_3.if_203.cpu_write_3.i[0]
	<-
	CLK  glob.c32
	D    S1071[0]
	CE   SB1050
	R    TS1037
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.in_r.D[0..5] = prog.read_3.case_21.read_value_3.in_1[0..5]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.in_r[0..5]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.in_r.D[0..5]
	CE   VCC
	R    GND
    {0x0}
OR2629 = TS1127 OR SD1197
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.data.D[0..7]
	<-
	SEL  TS1127
	IN   E1145[0..7]
	SEL  SD1197
	IN   E1190[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.data.D[0..7]
	CE   OR2629
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.empty[0]
	<-
	CLK  glob.c32
	D    E1156[0]
	CE   TS1127
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_84.read_value_gen_3.cpu_read_3.i[0]
	<-
	CLK  glob.c32
	D    S1186[0]
	CE   SD1197
	R    TS1127
    {0x0}
OR2636 = SD1249 OR F1240
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.data.D[0..15]
	<-
	SEL  F1240
	IN   E1262[0..15]
	SEL  SD1249
	IN   E1241[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.data[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.data.D[0..15]
	CE   OR2636
	R    TS1222
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.i.D[0..1] = S1256[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.i[0..1]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.cpu_write_4.i.D[0..1]
	CE   SB1235
	R    TS1222
    {0x0}
OR2641 = SD1377 OR TS1307
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.data.D[0..7]
	<-
	SEL  TS1307
	IN   E1325[0..7]
	SEL  SD1377
	IN   E1370[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.data.D[0..7]
	CE   OR2641
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.empty[0]
	<-
	CLK  glob.c32
	D    E1336[0]
	CE   TS1307
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_210.cpu_read_4.i[0]
	<-
	CLK  glob.c32
	D    S1366[0]
	CE   SD1377
	R    TS1307
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.ilevel.D[0..3] = E1454[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.ilevel[0..3]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.ilevel.D[0..3]
	CE   F1447
	R    GND
    {0x1}
OR2649 = SD1428 OR F1420
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.data.D[0..7]
	<-
	SEL  SD1428
	IN   E1421[0..7]
	SEL  F1420
	IN   E1441[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.data.D[0..7]
	CE   OR2649
	R    TS1402
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.cpu_write_5.i[0]
	<-
	CLK  glob.c32
	D    S1435[0]
	CE   SB1415
	R    TS1402
    {0x0}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.sigint_2.q[0..4]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.sigint_2.q.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_85.write_queue_gen_0.if_211.if_212.if_213.sigint_2.q.NF
	<-
	CLK      glob.c32
	DATA     1
	PUSH     TS1477
	POP      TS2317
	RESET    GND
}
OR2655 = SD1605 OR SD1534
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.data.D[0..15]
	<-
	SEL  SD1605
	IN   E1598[0..15]
	SEL  SD1534
	IN   E1527[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.data[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.data.D[0..15]
	CE   OR2655
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.empty[0]
	<-
	CLK  glob.c32
	D    E1563[0]
	CE   TS1509
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.i.D[0..1] = S1594[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.i[0..1]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.cpu_read_5.i.D[0..1]
	CE   SD1605
	R    TS1509
    {0x0}
OR2661 = TS1636 OR SD1706
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.data.D[0..7]
	<-
	SEL  TS1636
	IN   E1654[0..7]
	SEL  SD1706
	IN   E1699[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.data.D[0..7]
	CE   OR2661
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.empty[0]
	<-
	CLK  glob.c32
	D    E1665[0]
	CE   TS1636
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_218.cpu_read_6.i[0]
	<-
	CLK  glob.c32
	D    S1695[0]
	CE   SD1706
	R    TS1636
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.ilevel.D[0..3] = E1784[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.ilevel[0..3]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.ilevel.D[0..3]
	CE   F1777
	R    GND
    {0x1}
OR2669 = F1749 OR SD1758
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.data.D[0..7]
	<-
	SEL  SD1758
	IN   E1750[0..7]
	SEL  F1749
	IN   E1771[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.data[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.data.D[0..7]
	CE   OR2669
	R    TS1731
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.cpu_write_6.i[0]
	<-
	CLK  glob.c32
	D    S1765[0]
	CE   SB1744
	R    TS1731
    {0x0}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.sigint_3.q[0..4]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.sigint_3.q.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_86.read_queue_gen_0.if_219.if_220.if_221.sigint_3.q.NF
	<-
	CLK      glob.c32
	DATA     2
	PUSH     TS1807
	POP      TS2349
	RESET    GND
}
OR2675 = SD1870 OR SD1923
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.data.D[0..15]
	<-
	SEL  SD1870
	IN   E1863[0..15]
	SEL  SD1923
	IN   E1916[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.data[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.data.D[0..15]
	CE   OR2675
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.empty[0]
	<-
	CLK  glob.c32
	D    E1881[0]
	CE   TS1845
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.i.D[0..1] = S1912[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.i[0..1]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_87.read_queue_gen_1.cpu_read_7.i.D[0..1]
	CE   SD1923
	R    TS1845
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.mdata.D[0..15] = E2004[0..15]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.mdata[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.mdata.D[0..15]
	CE   F1997
	R    GND
    {0x0}
OR2683 = SD1978 OR F1969
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.data.D[0..15]
	<-
	SEL  F1969
	IN   E1991[0..15]
	SEL  SD1978
	IN   E1970[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.data[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.data.D[0..15]
	CE   OR2683
	R    TS1951
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.i.D[0..1] = S1985[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.i[0..1]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_88.write_memory_gen_0.cpu_write_7.i.D[0..1]
	CE   SB1964
	R    TS1951
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.data[0..15]
	<-
	CLK  glob.c32
	D    prog.m_0[0..15]
	CE   S2160
	R    GND
    {0x0}
OR2689 = F2064 OR SD2131
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.data.D[0..15]
	<-
	SEL  F2064
	IN   E2078[0..15]
	SEL  SD2131
	IN   E2124[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.data[0..15]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.data.D[0..15]
	CE   OR2689
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.empty[0]
	<-
	CLK  glob.c32
	D    E2089[0]
	CE   F2064
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.i.D[0..1] = S2120[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.i[0..1]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_177.forvar_91.for_89.read_memory_gen_0.if_230.cpu_read_8.i.D[0..1]
	CE   SD2131
	R    F2064
    {0x0}
OR2695 = TS2349 OR TS2317
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.D[0..6]
	<-
	SEL  TS2317
	IN   S2332[0..6]
	SEL  TS2349
	IN   S2362[0..6]
    unselected out 0x0
}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2[0..6]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.NF
	<-
	CLK      glob.c32
	DATA     FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.a2.D[0..6]
	PUSH     OR2695
	POP      TS2288
	RESET    GND
}
OR2698 = TS2213 OR TS2181
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.D[0..6]
	<-
	SEL  TS2181
	IN   S2196[0..6]
	SEL  TS2213
	IN   S2226[0..6]
    unselected out 0x0
}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1[0..6]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.NF
	<-
	CLK      glob.c32
	DATA     FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.a1.D[0..6]
	PUSH     OR2698
	POP      TS2257
	RESET    GND
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_234.multiplext__1.case_38.select1[0]
	<-
	CLK  glob.c32
	D    E2249[0]
	CE   TS2243
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.multiplext__2.case_43.select1[0]
	<-
	CLK  glob.c32
	D    E2385[0]
	CE   TS2379
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_233.multiplext_0.multiplext__0.case_37.if_235.s1[0]
	<-
	CLK  glob.c32
	D    E2399[0]
	CE   TS2393
	R    GND
    {0x0}
RRAM - 1 ports {
	OUT0	FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.im_0[0..31]
	<-
	CLK0	glob.c32
	ADDR0	MADDR2478[0..7]
	DATA0	-
	RE0	TS2471
	WE0	-
    initialised
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address.D[0..7] = S2492[0..7]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7]
	<-
	CLK  glob.c32
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address.D[0..7]
	CE   TS2482
	R    TS2452
    {0x0}
DFF FDRSE {
	OUT      SDD2705
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2706
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2707
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2708
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2709
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2710
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2711
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2712
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2713
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2714
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2715
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2716
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2717
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2718
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2719
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2720
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2721
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2722
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2723
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2724
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
DFF FDRSE {
	OUT      SDD2725
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD1945
init = R
}
IBUF  prog.FMOD1/papilio_one_500.c32_in <- PORT_P89 loc=P89 id b2 IBUFG
START { 
	OUT  glob.c32.start
	<-
	IN   VCC
	CLK  glob.c32
}


---------------------------------------------------------------------------
end of TDEList



