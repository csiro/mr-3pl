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
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4
	date             = 2021-10-26 14:32:54 +1100
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	unassOut         = fatal
	version          = 11.5.1M (devel svn 9947M, dun202)


3PL version 11.5.1M (devel svn 9947M, dun202).
Source file /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/prog.3pl
command line options - rntfs
2021-10-26 14:32:54 +1100

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
        FMOD10    /Users/dun202/src/mine/3pl/sources/include   uarts_c.3pl

Directives - final values.
-------------------------

	ALU              = false
	FIFO             = false
	OS               = mac os x
	QUEUEREG         = false
	arch             = x86_64
	comms_baud_rate  = 115200
	compileOnly      = false
	compilerMakeDate = 2021-08-06 15:23:59 +1000
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4
	date             = 2021-10-26 14:32:54 +1100
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/
	part             = xc3s500evq100-4
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.5.1/classtest4/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	srlAddrWidth     = 4
	uart_cpu         = true
	unassOut         = fatal
	version          = 11.5.1M (devel svn 9947M, dun202)




TDEList after all optimisation
------------------------------


ELEMENT DCM block dcm0
	PIN PSCLK C GND
	PIN CLKIN I glob.c32_in
	PIN CLKFB I glob.c32
	PIN RST I GND
	PIN PSINCDEC I GND
	PIN PSEN I GND
	PIN CLK0 O prog.FMOD1/papilio_one_500.simpleclock_0.craw
	property CLKIN_PERIOD = 31.25
ELEMENT BUFG block e0
	PIN I I prog.FMOD1/papilio_one_500.simpleclock_0.craw
	PIN O O glob.c32
OP E6[0..14] = prog.uart_rx_0.brc16_0.l[0..14] >> 14	(unsigned, unsigned)
E7[0] = E6[0..14] cast - pad
prog.uart_rx_0.brc16_0.to_pulse_0.in_1[0] = E7[0]
OP E14[0] = ~prog.uart_rx_0.brc16_0.to_pulse_0.prev[0]	(unsigned)
E15[0] = prog.uart_rx_0.brc16_0.to_pulse_0.in_1[0] AND E14[0]
OP E19[0..15] = prog.uart_rx_0.brc16_0.l[0..14] + 157	(unsigned, unsigned)
S22[0..14] = E19[0..15] cast - pad
OP E28[0] = ~prog.q1.NF	(unsigned)
OP E38[0..4] = prog.uart_rx_0.brc_count[0..3] + 1	(unsigned, unsigned)
S41[0..3] = E38[0..4] cast - pad
WHEN {
	T_START  TS31
	F_START  FS32
	FINISH   -
	<-
	START    SDD695
	TEST     E15[0]
	T_FINISH -
	F_FINISH -
}
OP E60[0] = prog.uart_rx_0.brc_count[0..3] == 0	(unsigned, unsigned)
E61[0] = E15[0] AND E60[0]
OP E67[0] = ~prog.uart_rx_0.rxd16[0]	(unsigned)
E68[0] = E67[0] AND prog.uart_rx_0.rxd16_prev[0]
DEL F72 <- TS64 CLK glob.c32 delay 1
OP E75[0] = ~E61[0]	(unsigned)
DEL FB76 <- SB74 CLK glob.c32 delay 1
WHILE {
	START_B    SB74
	FINISH     F79
	<-
	START      F72
	TEST       E75[0]
	CONTIN     FB76
	C          glob.c32
	RESET      null
}
DEL F85 <- F79 CLK glob.c32 delay 1
OP E88[0] = ~E61[0]	(unsigned)
DEL FB89 <- SB87 CLK glob.c32 delay 1
WHILE {
	START_B    SB87
	FINISH     F92
	<-
	START      F85
	TEST       E88[0]
	CONTIN     FB89
	C          glob.c32
	RESET      null
}
DEL F98 <- F92 CLK glob.c32 delay 1
OP E101[0] = prog.uart_rx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E104[0] = ~E61[0]	(unsigned)
DEL FB105 <- SB103 CLK glob.c32 delay 1
WHILE {
	START_B    SB103
	FINISH     F108
	<-
	START      SB100
	TEST       E104[0]
	CONTIN     FB105
	C          glob.c32
	RESET      null
}
OP E116[0..8] = prog.uart_rx_0.data[0..7] << 1	(unsigned, unsigned)
E117[0] = prog.uart_rx_0.rxd16[0] cast - pad
OP E118[0..8] = E116[0..8] | E117[0]	(unsigned, unsigned)
S121[0..7] = E118[0..8] cast - pad
DEL F115 <- F108 CLK glob.c32 delay 1
OP E125[0..4] = prog.uart_rx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S128[0..3] = E125[0..4] cast - pad
WHILE {
	START_B    SB100
	FINISH     F134
	<-
	START      F98
	TEST       E101[0]
	CONTIN     F115
	C          glob.c32
	RESET      null
}
OP E140[0] = ~E61[0]	(unsigned)
DEL FB141 <- SB139 CLK glob.c32 delay 1
WHILE {
	START_B    SB139
	FINISH     F144
	<-
	START      F134
	TEST       E140[0]
	CONTIN     FB141
	C          glob.c32
	RESET      null
}
OP E151[0] = ~prog.uart_rx_0.rxd16[0]	(unsigned)
DEL F150 <- F144 CLK glob.c32 delay 1
prog.uart_rx_0.seq_0.reverse_0.in_1[0..7] = prog.uart_rx_0.data[0..7]
E166[0,1,2,3,4,5,6,7] = prog.uart_rx_0.seq_0.reverse_0.in_1[0..7] cast - pad
S167[0] = E166[0] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[0] = S167[0]
S168[0] = E166[1] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[1] = S168[0]
S169[0] = E166[2] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[2] = S169[0]
S170[0] = E166[3] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[3] = S170[0]
S171[0] = E166[4] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[4] = S171[0]
S172[0] = E166[5] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[5] = S172[0]
S173[0] = E166[6] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[6] = S173[0]
S174[0] = E166[7] cast - pad
prog.uart_rx_0.seq_0.reverse_0.i_1[7] = S174[0]
prog.uart_rx_0.seq_0.reverse_0.o_1[0] = prog.uart_rx_0.seq_0.reverse_0.i_1[7]
prog.uart_rx_0.seq_0.reverse_0.o_2[1] = prog.uart_rx_0.seq_0.reverse_0.i_1[6]
prog.uart_rx_0.seq_0.reverse_0.o_3[2] = prog.uart_rx_0.seq_0.reverse_0.i_1[5]
prog.uart_rx_0.seq_0.reverse_0.o_4[3] = prog.uart_rx_0.seq_0.reverse_0.i_1[4]
prog.uart_rx_0.seq_0.reverse_0.o_5[4] = prog.uart_rx_0.seq_0.reverse_0.i_1[3]
prog.uart_rx_0.seq_0.reverse_0.o_6[5] = prog.uart_rx_0.seq_0.reverse_0.i_1[2]
prog.uart_rx_0.seq_0.reverse_0.o_7[6] = prog.uart_rx_0.seq_0.reverse_0.i_1[1]
prog.uart_rx_0.seq_0.reverse_0.o_8[7] = prog.uart_rx_0.seq_0.reverse_0.i_1[0]
prog.uart_rx_0.seq_0.reverse_0.o_9[0] = prog.uart_rx_0.seq_0.reverse_0.o_1[0] cast - pad
prog.uart_rx_0.seq_0.reverse_0.o_9[1] = prog.uart_rx_0.seq_0.reverse_0.o_2[1] cast - pad
prog.uart_rx_0.seq_0.reverse_0.o_9[2] = prog.uart_rx_0.seq_0.reverse_0.o_3[2] cast - pad
prog.uart_rx_0.seq_0.reverse_0.o_9[3] = prog.uart_rx_0.seq_0.reverse_0.o_4[3] cast - pad
prog.uart_rx_0.seq_0.reverse_0.o_9[4] = prog.uart_rx_0.seq_0.reverse_0.o_5[4] cast - pad
prog.uart_rx_0.seq_0.reverse_0.o_9[5] = prog.uart_rx_0.seq_0.reverse_0.o_6[5] cast - pad
prog.uart_rx_0.seq_0.reverse_0.o_9[6] = prog.uart_rx_0.seq_0.reverse_0.o_7[6] cast - pad
prog.uart_rx_0.seq_0.reverse_0.o_9[7] = prog.uart_rx_0.seq_0.reverse_0.o_8[7] cast - pad
E199[0..7] = prog.uart_rx_0.seq_0.reverse_0.o_9[0,1,2,3,4,5,6,7] cast - pad
E200[0..7] = E199[0..7] cast - pad
DEL F155 <- F150 CLK glob.c32 delay 1
WHEN {
	T_START  TS156
	F_START  FS157
	FINISH   -
	<-
	START    F150
	TEST     prog.q1.NF
	T_FINISH -
	F_FINISH -
}
DEL FF211 <- FS65 CLK glob.c32 delay 1
WHEN {
	T_START  TS64
	F_START  FS65
	FINISH   F63
	<-
	START    S62
	TEST     E68[0]
	T_FINISH F155
	F_FINISH FF211
}
ILOOP  S62 <- SSD523 F63
E214[0..7] = prog.q1[0..7] cast - pad
DEL E221[0] <- E221[0] CLK glob.c32 delay 16 EN E15[0] initialised
E222[0] = E221[0] AND E15[0]
prog.tx1_1[0] = prog.uart_tx_0.txd_[0]
prog.tx1_2[0] = prog.uart_tx_0.txd_[0]
OP E236[0] = ~E222[0]	(unsigned)
DEL FB239 <- SB238 CLK glob.c32 delay 1
WHILE {
	START_B    SB238
	FINISH     F242
	<-
	START      TS228
	TEST       E236[0]
	CONTIN     FB239
	C          glob.c32
	RESET      null
}
DEL F248 <- SD272 CLK glob.c32 delay 1
EXECP no priority, buffered queues only {
	START_DEL SD272
	<-
	CLK       glob.c32
	START_IN  F242
	BQAV      prog.q1.NE
}
OP E275[0] = prog.uart_tx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E276[0] = ~E222[0]	(unsigned)
DEL FB279 <- SB278 CLK glob.c32 delay 1
WHILE {
	START_B    SB278
	FINISH     F282
	<-
	START      SB274
	TEST       E276[0]
	CONTIN     FB279
	C          glob.c32
	RESET      null
}
E289[0] = prog.uart_tx_0.data[0..7] cast - pad
DEL F288 <- F282 CLK glob.c32 delay 1
OP E295[0..7] = prog.uart_tx_0.data[0..7] >> 1	(unsigned, unsigned)
DEL F294 <- F288 CLK glob.c32 delay 1
OP E301[0..4] = prog.uart_tx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S304[0..3] = E301[0..4] cast - pad
DEL F300 <- F294 CLK glob.c32 delay 1
WHILE {
	START_B    SB274
	FINISH     F308
	<-
	START      F248
	TEST       E275[0]
	CONTIN     F300
	C          glob.c32
	RESET      null
}
OP E312[0] = ~E222[0]	(unsigned)
DEL FB315 <- SB314 CLK glob.c32 delay 1
WHILE {
	START_B    SB314
	FINISH     F318
	<-
	START      F308
	TEST       E312[0]
	CONTIN     FB315
	C          glob.c32
	RESET      null
}
DEL F324 <- F318 CLK glob.c32 delay 1
DEL F328 <- FS229 CLK glob.c32 delay 1
WHEN {
	T_START  TS228
	F_START  FS229
	FINISH   F227
	<-
	START    S226
	TEST     prog.q1.NE
	T_FINISH F324
	F_FINISH F328
}
ILOOP  S226 <- SSD523 F227
OP E338[0] = ~prog.q2.NF	(unsigned)
OP E348[0..4] = prog.uart_rx_1.brc_count[0..3] + 1	(unsigned, unsigned)
S351[0..3] = E348[0..4] cast - pad
WHEN {
	T_START  TS341
	F_START  FS342
	FINISH   -
	<-
	START    SDD696
	TEST     E15[0]
	T_FINISH -
	F_FINISH -
}
OP E370[0] = prog.uart_rx_1.brc_count[0..3] == 0	(unsigned, unsigned)
E371[0] = E15[0] AND E370[0]
OP E377[0] = ~prog.uart_rx_1.rxd16[0]	(unsigned)
E378[0] = E377[0] AND prog.uart_rx_1.rxd16_prev[0]
DEL F382 <- TS374 CLK glob.c32 delay 1
OP E385[0] = ~E371[0]	(unsigned)
DEL FB386 <- SB384 CLK glob.c32 delay 1
WHILE {
	START_B    SB384
	FINISH     F389
	<-
	START      F382
	TEST       E385[0]
	CONTIN     FB386
	C          glob.c32
	RESET      null
}
DEL F395 <- F389 CLK glob.c32 delay 1
OP E398[0] = ~E371[0]	(unsigned)
DEL FB399 <- SB397 CLK glob.c32 delay 1
WHILE {
	START_B    SB397
	FINISH     F402
	<-
	START      F395
	TEST       E398[0]
	CONTIN     FB399
	C          glob.c32
	RESET      null
}
DEL F408 <- F402 CLK glob.c32 delay 1
OP E411[0] = prog.uart_rx_1.bitcount[0..3] != 0	(unsigned, unsigned)
OP E414[0] = ~E371[0]	(unsigned)
DEL FB415 <- SB413 CLK glob.c32 delay 1
WHILE {
	START_B    SB413
	FINISH     F418
	<-
	START      SB410
	TEST       E414[0]
	CONTIN     FB415
	C          glob.c32
	RESET      null
}
OP E426[0..8] = prog.uart_rx_1.data[0..7] << 1	(unsigned, unsigned)
E427[0] = prog.uart_rx_1.rxd16[0] cast - pad
OP E428[0..8] = E426[0..8] | E427[0]	(unsigned, unsigned)
S431[0..7] = E428[0..8] cast - pad
DEL F425 <- F418 CLK glob.c32 delay 1
OP E435[0..4] = prog.uart_rx_1.bitcount[0..3] - 1	(unsigned, unsigned)
S438[0..3] = E435[0..4] cast - pad
WHILE {
	START_B    SB410
	FINISH     F444
	<-
	START      F408
	TEST       E411[0]
	CONTIN     F425
	C          glob.c32
	RESET      null
}
OP E450[0] = ~E371[0]	(unsigned)
DEL FB451 <- SB449 CLK glob.c32 delay 1
WHILE {
	START_B    SB449
	FINISH     F454
	<-
	START      F444
	TEST       E450[0]
	CONTIN     FB451
	C          glob.c32
	RESET      null
}
OP E461[0] = ~prog.uart_rx_1.rxd16[0]	(unsigned)
DEL F460 <- F454 CLK glob.c32 delay 1
prog.uart_rx_1.seq_11.reverse_1.in_1[0..7] = prog.uart_rx_1.data[0..7]
E476[0,1,2,3,4,5,6,7] = prog.uart_rx_1.seq_11.reverse_1.in_1[0..7] cast - pad
S477[0] = E476[0] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[0] = S477[0]
S478[0] = E476[1] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[1] = S478[0]
S479[0] = E476[2] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[2] = S479[0]
S480[0] = E476[3] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[3] = S480[0]
S481[0] = E476[4] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[4] = S481[0]
S482[0] = E476[5] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[5] = S482[0]
S483[0] = E476[6] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[6] = S483[0]
S484[0] = E476[7] cast - pad
prog.uart_rx_1.seq_11.reverse_1.i_1[7] = S484[0]
prog.uart_rx_1.seq_11.reverse_1.o_1[0] = prog.uart_rx_1.seq_11.reverse_1.i_1[7]
prog.uart_rx_1.seq_11.reverse_1.o_2[1] = prog.uart_rx_1.seq_11.reverse_1.i_1[6]
prog.uart_rx_1.seq_11.reverse_1.o_3[2] = prog.uart_rx_1.seq_11.reverse_1.i_1[5]
prog.uart_rx_1.seq_11.reverse_1.o_4[3] = prog.uart_rx_1.seq_11.reverse_1.i_1[4]
prog.uart_rx_1.seq_11.reverse_1.o_5[4] = prog.uart_rx_1.seq_11.reverse_1.i_1[3]
prog.uart_rx_1.seq_11.reverse_1.o_6[5] = prog.uart_rx_1.seq_11.reverse_1.i_1[2]
prog.uart_rx_1.seq_11.reverse_1.o_7[6] = prog.uart_rx_1.seq_11.reverse_1.i_1[1]
prog.uart_rx_1.seq_11.reverse_1.o_8[7] = prog.uart_rx_1.seq_11.reverse_1.i_1[0]
prog.uart_rx_1.seq_11.reverse_1.o_9[0] = prog.uart_rx_1.seq_11.reverse_1.o_1[0] cast - pad
prog.uart_rx_1.seq_11.reverse_1.o_9[1] = prog.uart_rx_1.seq_11.reverse_1.o_2[1] cast - pad
prog.uart_rx_1.seq_11.reverse_1.o_9[2] = prog.uart_rx_1.seq_11.reverse_1.o_3[2] cast - pad
prog.uart_rx_1.seq_11.reverse_1.o_9[3] = prog.uart_rx_1.seq_11.reverse_1.o_4[3] cast - pad
prog.uart_rx_1.seq_11.reverse_1.o_9[4] = prog.uart_rx_1.seq_11.reverse_1.o_5[4] cast - pad
prog.uart_rx_1.seq_11.reverse_1.o_9[5] = prog.uart_rx_1.seq_11.reverse_1.o_6[5] cast - pad
prog.uart_rx_1.seq_11.reverse_1.o_9[6] = prog.uart_rx_1.seq_11.reverse_1.o_7[6] cast - pad
prog.uart_rx_1.seq_11.reverse_1.o_9[7] = prog.uart_rx_1.seq_11.reverse_1.o_8[7] cast - pad
E509[0..7] = prog.uart_rx_1.seq_11.reverse_1.o_9[0,1,2,3,4,5,6,7] cast - pad
E510[0..7] = E509[0..7] cast - pad
DEL F465 <- F460 CLK glob.c32 delay 1
WHEN {
	T_START  TS466
	F_START  FS467
	FINISH   -
	<-
	START    F460
	TEST     prog.q2.NF
	T_FINISH -
	F_FINISH -
}
DEL FF521 <- FS375 CLK glob.c32 delay 1
WHEN {
	T_START  TS374
	F_START  FS375
	FINISH   F373
	<-
	START    S372
	TEST     E378[0]
	T_FINISH F465
	F_FINISH FF521
}
DEL SSD523 <- glob.c32.start CLK glob.c32 delay 1
ILOOP  S372 <- SSD523 F373
E524[0..7] = prog.q2[0..7] cast - pad
DEL E531[0] <- E531[0] CLK glob.c32 delay 16 EN E15[0] initialised
E532[0] = E531[0] AND E15[0]
prog.tx2_1[0] = prog.uart_tx_1.txd_[0]
prog.tx2_2[0] = prog.uart_tx_1.txd_[0]
OP E546[0] = ~E532[0]	(unsigned)
DEL FB549 <- SB548 CLK glob.c32 delay 1
WHILE {
	START_B    SB548
	FINISH     F552
	<-
	START      TS538
	TEST       E546[0]
	CONTIN     FB549
	C          glob.c32
	RESET      null
}
DEL F558 <- SD582 CLK glob.c32 delay 1
EXECP no priority, buffered queues only {
	START_DEL SD582
	<-
	CLK       glob.c32
	START_IN  F552
	BQAV      prog.q2.NE
}
OP E585[0] = prog.uart_tx_1.bitcount[0..3] != 0	(unsigned, unsigned)
OP E586[0] = ~E532[0]	(unsigned)
DEL FB589 <- SB588 CLK glob.c32 delay 1
WHILE {
	START_B    SB588
	FINISH     F592
	<-
	START      SB584
	TEST       E586[0]
	CONTIN     FB589
	C          glob.c32
	RESET      null
}
E599[0] = prog.uart_tx_1.data[0..7] cast - pad
DEL F598 <- F592 CLK glob.c32 delay 1
OP E605[0..7] = prog.uart_tx_1.data[0..7] >> 1	(unsigned, unsigned)
DEL F604 <- F598 CLK glob.c32 delay 1
OP E611[0..4] = prog.uart_tx_1.bitcount[0..3] - 1	(unsigned, unsigned)
S614[0..3] = E611[0..4] cast - pad
DEL F610 <- F604 CLK glob.c32 delay 1
WHILE {
	START_B    SB584
	FINISH     F618
	<-
	START      F558
	TEST       E585[0]
	CONTIN     F610
	C          glob.c32
	RESET      null
}
OP E622[0] = ~E532[0]	(unsigned)
DEL FB625 <- SB624 CLK glob.c32 delay 1
WHILE {
	START_B    SB624
	FINISH     F628
	<-
	START      F618
	TEST       E622[0]
	CONTIN     FB625
	C          glob.c32
	RESET      null
}
DEL F634 <- F628 CLK glob.c32 delay 1
DEL F638 <- FS539 CLK glob.c32 delay 1
WHEN {
	T_START  TS538
	F_START  FS539
	FINISH   F537
	<-
	START    S536
	TEST     prog.q2.NE
	T_FINISH F634
	F_FINISH F638
}
ILOOP  S536 <- SSD523 F537
NCF "NET PORT_P89 IOSTANDARD=LVCMOS33;" port PORT_P89
NCF "NET %n TNM=C32;" port null
NCF "TIMESPEC TS_C32=PERIOD C32 31.25;" port null
NCF "NET PORT_P18 IOSTANDARD=LVCMOS33;" port PORT_P18
NCF "NET PORT_P23 IOSTANDARD=LVCMOS33;" port PORT_P23
NCF "NET PORT_P85 IOSTANDARD=LVCMOS33;" port PORT_P85
NCF "NET PORT_P83 IOSTANDARD=LVCMOS33;" port PORT_P83
prog.OUTPUT0[0] = prog.tx1_1[0]
OBUF PORT_P18 <- OUTPUTBIT641[0] loc=P18 id b26 OBUF
OUTPUTBIT641[0] = prog.OUTPUT0[0] cast - pad
prog.OUTPUT1[0] = prog.tx2_1[0]
OBUF PORT_P23 <- OUTPUTBIT642[0] loc=P23 id b27 OBUF
OUTPUTBIT642[0] = prog.OUTPUT1[0] cast - pad
IBUF  INPUT643[0] <- PORT_P85 loc=P85 id b28 IBUF
IBUF  INPUT645[0] <- PORT_P83 loc=P83 id b29 IBUF
prog.q1.D[0..7] = E200[0..7]
QUEUEBUFFER  depth 2 {
	OUT      prog.q1[0..7]
	NE       prog.q1.NE
	NF       prog.q1.NF
	<-
	CLK      glob.c32
	DATA     prog.q1.D[0..7]
	PUSH     TS156
	POP      SD272
	RESET    GND
}
prog.q2.D[0..7] = E510[0..7]
QUEUEBUFFER  depth 2 {
	OUT      prog.q2[0..7]
	NE       prog.q2.NE
	NF       prog.q2.NF
	<-
	CLK      glob.c32
	DATA     prog.q2.D[0..7]
	PUSH     TS466
	POP      SD582
	RESET    GND
}
prog.uart_rx_0.brc16_0.l.D[0..14] = S22[0..14]
REG
	OUT  prog.uart_rx_0.brc16_0.l[0..14]
	<-
	CLK  glob.c32
	D    prog.uart_rx_0.brc16_0.l.D[0..14]
	CE   SDD694
	R    GND
    {0x0}
REG
	OUT  prog.uart_rx_0.brc16_0.to_pulse_0.prev[0]
	<-
	CLK  glob.c32
	D    prog.uart_rx_0.brc16_0.to_pulse_0.in_1[0]
	CE   SDD693
	R    GND
    {0x1}
prog.uart_rx_0.data.D[0..7] = S121[0..7]
REG
	OUT  prog.uart_rx_0.data[0..7]
	<-
	CLK  glob.c32
	D    prog.uart_rx_0.data.D[0..7]
	CE   F108
	R    GND
    {0x0}
prog.uart_rx_0.bitcount.D[0..3] = S128[0..3]
REG
	OUT  prog.uart_rx_0.bitcount[0..3]
	<-
	CLK  glob.c32
	D    prog.uart_rx_0.bitcount.D[0..3]
	CE   F108
	R    TS64
    {0x8}
REG
	OUT  prog.uart_rx_0.ferr[0]
	<-
	CLK  glob.c32
	D    E151[0]
	CE   F144
	R    TS64
    {0x0}
REG
	OUT  prog.uart_rx_0.oflo[0]
	<-
	CLK  glob.c32
	D    VCC
	CE   FS157
	R    TS64
    {0x0}
OR658 = F79 OR TS64 OR F92
prog.uart_rx_0.brc_count.D[0..3] = S41[0..3]
REG
	OUT  prog.uart_rx_0.brc_count[0..3]
	<-
	CLK  glob.c32
	D    prog.uart_rx_0.brc_count.D[0..3]
	CE   TS31
	R    OR658
    {0x9}
REG
	OUT  prog.uart_rx_0.rxd16[0]
	<-
	CLK  glob.c32
	D    INPUT643[0]
	CE   TS31
	R    GND
    {0x0}
REG
	OUT  prog.uart_rx_0.rxd16_prev[0]
	<-
	CLK  glob.c32
	D    prog.uart_rx_0.rxd16[0]
	CE   TS31
	R    GND
    {0x0}
OR665 = F282 OR SD272
SELECT {
	OUT  prog.uart_tx_0.txd_.D[0]
	<-
	SEL  F282
	IN   E289[0]
    unselected out 0x0
}
REG
	OUT  prog.uart_tx_0.txd_[0]
	<-
	CLK  glob.c32
	D    prog.uart_tx_0.txd_.D[0]
	CE   OR665
	R    F318
    {0x1}
OR666 = FS229 OR SD272
REG
	OUT  prog.uart_tx_0.txbusy_[0]
	<-
	CLK  glob.c32
	D    GND
	CE   GND
	R    OR666
    {0x1}
prog.uart_tx_0.bitcount.D[0..3] = S304[0..3]
REG
	OUT  prog.uart_tx_0.bitcount[0..3]
	<-
	CLK  glob.c32
	D    prog.uart_tx_0.bitcount.D[0..3]
	CE   F294
	R    SD272
    {0x8}
OR671 = SD272 OR F288
SELECT {
	OUT  prog.uart_tx_0.data.D[0..7]
	<-
	SEL  F288
	IN   E295[0..7]
	SEL  SD272
	IN   E214[0..7]
    unselected out 0x0
}
REG
	OUT  prog.uart_tx_0.data[0..7]
	<-
	CLK  glob.c32
	D    prog.uart_tx_0.data.D[0..7]
	CE   OR671
	R    GND
    {0x0}
prog.uart_rx_1.data.D[0..7] = S431[0..7]
REG
	OUT  prog.uart_rx_1.data[0..7]
	<-
	CLK  glob.c32
	D    prog.uart_rx_1.data.D[0..7]
	CE   F418
	R    GND
    {0x0}
prog.uart_rx_1.bitcount.D[0..3] = S438[0..3]
REG
	OUT  prog.uart_rx_1.bitcount[0..3]
	<-
	CLK  glob.c32
	D    prog.uart_rx_1.bitcount.D[0..3]
	CE   F418
	R    TS374
    {0x8}
REG
	OUT  prog.uart_rx_1.ferr[0]
	<-
	CLK  glob.c32
	D    E461[0]
	CE   F454
	R    TS374
    {0x0}
REG
	OUT  prog.uart_rx_1.oflo[0]
	<-
	CLK  glob.c32
	D    VCC
	CE   FS467
	R    TS374
    {0x0}
OR679 = F389 OR F402 OR TS374
prog.uart_rx_1.brc_count.D[0..3] = S351[0..3]
REG
	OUT  prog.uart_rx_1.brc_count[0..3]
	<-
	CLK  glob.c32
	D    prog.uart_rx_1.brc_count.D[0..3]
	CE   TS341
	R    OR679
    {0x9}
REG
	OUT  prog.uart_rx_1.rxd16[0]
	<-
	CLK  glob.c32
	D    INPUT645[0]
	CE   TS341
	R    GND
    {0x0}
REG
	OUT  prog.uart_rx_1.rxd16_prev[0]
	<-
	CLK  glob.c32
	D    prog.uart_rx_1.rxd16[0]
	CE   TS341
	R    GND
    {0x0}
OR686 = F592 OR SD582
SELECT {
	OUT  prog.uart_tx_1.txd_.D[0]
	<-
	SEL  F592
	IN   E599[0]
    unselected out 0x0
}
REG
	OUT  prog.uart_tx_1.txd_[0]
	<-
	CLK  glob.c32
	D    prog.uart_tx_1.txd_.D[0]
	CE   OR686
	R    F628
    {0x1}
OR687 = FS539 OR SD582
REG
	OUT  prog.uart_tx_1.txbusy_[0]
	<-
	CLK  glob.c32
	D    GND
	CE   GND
	R    OR687
    {0x1}
prog.uart_tx_1.bitcount.D[0..3] = S614[0..3]
REG
	OUT  prog.uart_tx_1.bitcount[0..3]
	<-
	CLK  glob.c32
	D    prog.uart_tx_1.bitcount.D[0..3]
	CE   F604
	R    SD582
    {0x8}
OR692 = SD582 OR F598
SELECT {
	OUT  prog.uart_tx_1.data.D[0..7]
	<-
	SEL  SD582
	IN   E524[0..7]
	SEL  F598
	IN   E605[0..7]
    unselected out 0x0
}
REG
	OUT  prog.uart_tx_1.data[0..7]
	<-
	CLK  glob.c32
	D    prog.uart_tx_1.data.D[0..7]
	CE   OR692
	R    GND
    {0x0}
DFF FDRSE {
	OUT      SDD693
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD523
init = R
}
DFF FDRSE {
	OUT      SDD694
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD523
init = R
}
DFF FDRSE {
	OUT      SDD695
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD523
init = R
}
DFF FDRSE {
	OUT      SDD696
	<-
	D        GND
	C        glob.c32
	CE       GND
	R        GND
	S        SSD523
init = R
}
IBUF  glob.c32_in <- PORT_P89 loc=P89 id b2 IBUFG
START { 
	OUT  glob.c32.start
	<-
	IN   VCC
	CLK  glob.c32
}


---------------------------------------------------------------------------
end of TDEList



