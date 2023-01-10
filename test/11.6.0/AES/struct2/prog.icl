Directives - initial values.
----------------------------

	ALU              = false
	FIFO             = false
	OS               = mac os x
	QUEUEREG         = false
	arch             = x86_64
	compileOnly      = false
	compilerMakeDate = 2022-08-24 14:24:48 +1000
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2
	date             = 2022-10-05 11:14:43 +1100
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	unassOut         = fatal
	version          = 11.7.0M (devel svn 10083:10129M, dun202)


3PL version 11.7.0M (devel svn 10083:10129M, dun202).
Source file /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/prog.3pl
command line options - rntfs
2022-10-05 11:14:43 +1100

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
	compilerMakeDate = 2022-08-24 14:24:48 +1000
	continuous       = false
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2
	date             = 2022-10-05 11:14:43 +1100
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/
	part             = xc3s400aft256-4
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.6.0/AES/struct2/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	srlAddrWidth     = 4
	tlimit           = 1016
	uart_cpu         = true
	unassOut         = fatal
	version          = 11.7.0M (devel svn 10083:10129M, dun202)




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
S3[0..8] = prog.s[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_1[0..8] = S3[0..8]
S4[0..11] = prog.s[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_1[9..20] = S4[0..11]
S5[0] = prog.s[21] cast - pad
prog.read_1.case_25.read_value_1.in_1[21] = S5[0]
S6[0..11] = prog.s[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_1[22..33] = S6[0..11]
S7[0] = prog.s[34] cast - pad
prog.read_1.case_25.read_value_1.in_1[34] = S7[0]
S8[0..15] = prog.s[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_1[35..50] = S8[0..15]
S9[0..15] = prog.s[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_1[51..66] = S9[0..15]
S10[0..15] = prog.s[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_1[67..82] = S10[0..15]
prog.read_1.case_25.read_value_1.in_2[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_2[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_2[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_2[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_2[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_2[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_2[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_2[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_3[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_3[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_3[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_3[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_3[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_3[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_3[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_3[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
OP E14[0..10] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..10] >> 10	(unsigned, unsigned)
E15[0] = E14[0..10] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0] = E15[0]
OP E22[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.prev[0]	(unsigned)
E23[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0] AND E22[0]
OP E27[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..10] + 109	(unsigned, unsigned)
S30[0..10] = E27[0..11] cast - pad
OP E36[0] = ~FMOD7/uart_cpu.rx.NF	(unsigned)
OP E46[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3] + 1	(unsigned, unsigned)
S49[0..3] = E46[0..4] cast - pad
WHEN {
	T_START  TS39
	F_START  FS40
	FINISH   -
	<-
	START    SDD922
	TEST     E23[0]
	T_FINISH -
	F_FINISH -
}
OP E68[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3] == 0	(unsigned, unsigned)
E69[0] = E23[0] AND E68[0]
OP E75[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]	(unsigned)
E76[0] = E75[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16_prev[0]
DEL F80 <- TS72 CLK glob.c16 delay 1
OP E83[0] = ~E69[0]	(unsigned)
DEL FB84 <- SB82 CLK glob.c16 delay 1
WHILE {
	START_B    SB82
	FINISH     F87
	<-
	START      F80
	TEST       E83[0]
	CONTIN     FB84
	C          glob.c16
	RESET      null
}
DEL F93 <- F87 CLK glob.c16 delay 1
OP E96[0] = ~E69[0]	(unsigned)
DEL FB97 <- SB95 CLK glob.c16 delay 1
WHILE {
	START_B    SB95
	FINISH     F100
	<-
	START      F93
	TEST       E96[0]
	CONTIN     FB97
	C          glob.c16
	RESET      null
}
DEL F106 <- F100 CLK glob.c16 delay 1
OP E109[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E112[0] = ~E69[0]	(unsigned)
DEL FB113 <- SB111 CLK glob.c16 delay 1
WHILE {
	START_B    SB111
	FINISH     F116
	<-
	START      SB108
	TEST       E112[0]
	CONTIN     FB113
	C          glob.c16
	RESET      null
}
OP E124[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7] << 1	(unsigned, unsigned)
E125[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0] cast - pad
OP E126[0..8] = E124[0..8] | E125[0]	(unsigned, unsigned)
S129[0..7] = E126[0..8] cast - pad
DEL F123 <- F116 CLK glob.c16 delay 1
OP E133[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S136[0..3] = E133[0..4] cast - pad
WHILE {
	START_B    SB108
	FINISH     F142
	<-
	START      F106
	TEST       E109[0]
	CONTIN     F123
	C          glob.c16
	RESET      null
}
OP E148[0] = ~E69[0]	(unsigned)
DEL FB149 <- SB147 CLK glob.c16 delay 1
WHILE {
	START_B    SB147
	FINISH     F152
	<-
	START      F142
	TEST       E148[0]
	CONTIN     FB149
	C          glob.c16
	RESET      null
}
OP E159[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]	(unsigned)
DEL F158 <- F152 CLK glob.c16 delay 1
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.in_1[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7]
E174[0,1,2,3,4,5,6,7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.in_1[0..7] cast - pad
S175[0] = E174[0] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[0] = S175[0]
S176[0] = E174[1] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[1] = S176[0]
S177[0] = E174[2] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[2] = S177[0]
S178[0] = E174[3] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[3] = S178[0]
S179[0] = E174[4] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[4] = S179[0]
S180[0] = E174[5] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[5] = S180[0]
S181[0] = E174[6] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[6] = S181[0]
S182[0] = E174[7] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[7] = S182[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_2[1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[6]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_3[2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[5]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_4[3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_5[4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[3]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_6[5] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[2]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_7[6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[1]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_8[7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.i_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_1[0] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_2[1] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_3[2] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_4[3] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_5[4] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[5] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_6[5] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_7[6] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_8[7] cast - pad
E207[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_0.reverse_0.o_9[0,1,2,3,4,5,6,7] cast - pad
E208[0..7] = E207[0..7] cast - pad
DEL F163 <- F158 CLK glob.c16 delay 1
WHEN {
	T_START  TS164
	F_START  FS165
	FINISH   -
	<-
	START    F158
	TEST     FMOD7/uart_cpu.rx.NF
	T_FINISH -
	F_FINISH -
}
DEL FF219 <- FS73 CLK glob.c16 delay 1
WHEN {
	T_START  TS72
	F_START  FS73
	FINISH   F71
	<-
	START    S70
	TEST     E76[0]
	T_FINISH F163
	F_FINISH FF219
}
ILOOP  S70 <- SSD469 F71
E222[0..7] = FMOD7/uart_cpu.tx[0..7] cast - pad
DEL E229[0] <- E229[0] CLK glob.c16 delay 16 EN E23[0] initialised
E230[0] = E229[0] AND E23[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.tx_out_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.txd_[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.tx_out_2[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.txd_[0]
OP E244[0] = ~E230[0]	(unsigned)
DEL FB247 <- SB246 CLK glob.c16 delay 1
WHILE {
	START_B    SB246
	FINISH     F250
	<-
	START      TS236
	TEST       E244[0]
	CONTIN     FB247
	C          glob.c16
	RESET      null
}
DEL F256 <- SD280 CLK glob.c16 delay 1
EXECP no priority, buffered queues only {
	START_DEL SD280
	<-
	CLK       glob.c16
	START_IN  F250
	BQAV      FMOD7/uart_cpu.tx.NE
}
OP E283[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E284[0] = ~E230[0]	(unsigned)
DEL FB287 <- SB286 CLK glob.c16 delay 1
WHILE {
	START_B    SB286
	FINISH     F290
	<-
	START      SB282
	TEST       E284[0]
	CONTIN     FB287
	C          glob.c16
	RESET      null
}
E297[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.data[0..7] cast - pad
DEL F296 <- F290 CLK glob.c16 delay 1
OP E303[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.data[0..7] >> 1	(unsigned, unsigned)
DEL F302 <- F296 CLK glob.c16 delay 1
OP E309[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S312[0..3] = E309[0..4] cast - pad
DEL F308 <- F302 CLK glob.c16 delay 1
WHILE {
	START_B    SB282
	FINISH     F316
	<-
	START      F256
	TEST       E283[0]
	CONTIN     F308
	C          glob.c16
	RESET      null
}
OP E320[0] = ~E230[0]	(unsigned)
DEL FB323 <- SB322 CLK glob.c16 delay 1
WHILE {
	START_B    SB322
	FINISH     F326
	<-
	START      F316
	TEST       E320[0]
	CONTIN     FB323
	C          glob.c16
	RESET      null
}
DEL F332 <- F326 CLK glob.c16 delay 1
OP E334[0] = ~E230[0]	(unsigned)
DEL FB337 <- SB336 CLK glob.c16 delay 1
WHILE {
	START_B    SB336
	FINISH     F340
	<-
	START      F332
	TEST       E334[0]
	CONTIN     FB337
	C          glob.c16
	RESET      null
}
DEL F346 <- F340 CLK glob.c16 delay 1
DEL F350 <- FS237 CLK glob.c16 delay 1
WHEN {
	T_START  TS236
	F_START  FS237
	FINISH   F235
	<-
	START    S234
	TEST     FMOD7/uart_cpu.tx.NE
	T_FINISH F346
	F_FINISH F350
}
ILOOP  S234 <- SSD469 F235
E361[0..5,6..7] = FMOD7/uart_cpu.rx[0..7] cast - pad
S364[0..5] = E361[0..5] cast - pad
S365[0..1] = E361[6..7] cast - pad
EXECP no priority, buffered queues only {
	START_DEL SD371
	<-
	CLK       glob.c16
	START_IN  TS355
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F360 <- SD371 CLK glob.c16 delay 1
DEL F374 <- F360 CLK glob.c16 delay 1
OP DO381 = FMOD7/uart_cpu.command[6..7] == 0	(unsigned, unsigned)
CS375 = F360 AND DO381
OP DO389 = FMOD7/uart_cpu.command[6..7] == 2	(unsigned, unsigned)
CS383 = F360 AND DO389
OP DO397 = FMOD7/uart_cpu.command[6..7] == 1	(unsigned, unsigned)
CS391 = F360 AND DO397
OP DO405 = FMOD7/uart_cpu.command[6..7] == 3	(unsigned, unsigned)
CS399 = F360 AND DO405
E417[0] = FMOD7/uart_cpu.rmem[0] OR FMOD7/uart_cpu.wmem[0]
E421[0..7] = FMOD7/uart_cpu.rx[0..7] cast - pad
S424[0..15] = E421[0..7] cast - pad
EXECP no priority, buffered queues only {
	START_DEL SD429
	<-
	CLK       glob.c16
	START_IN  TS414
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F420 <- SD429 CLK glob.c16 delay 1
E433[0..7] = FMOD7/uart_cpu.rx[0..7] cast - pad
OP E434[0..15] = E433[0..7] << 8	(unsigned, unsigned)
OP E435[0..15] = FMOD7/uart_cpu.memaddr[0..15] | E434[0..15]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD442
	<-
	CLK       glob.c16
	START_IN  F420
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F432 <- SD442 CLK glob.c16 delay 1
DEL FF444 <- FS415 CLK glob.c16 delay 1
WHEN {
	T_START  TS414
	F_START  FS415
	FINISH   F413
	<-
	START    F374
	TEST     E417[0]
	T_FINISH F432
	F_FINISH FF444
}
DEL F448 <- F413 CLK glob.c16 delay 1
DEL FF452 <- FS356 CLK glob.c16 delay 1
WHEN {
	T_START  TS355
	F_START  FS356
	FINISH   F354
	<-
	START    S353
	TEST     FMOD7/uart_cpu.idle[0]
	T_FINISH F448
	F_FINISH FF452
}
ILOOP  S353 <- SSD469 F354
prog.read_1.case_25.read_value_1.in_4[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_4[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_4[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_4[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_4[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_4[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_4[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_4[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_5[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_5[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_5[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_5[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_5[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_5[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_5[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_5[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
WHEN {
	T_START  TS460
	F_START  FS461
	FINISH   -
	<-
	START    SDD921
	TEST     prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforead[0]
	T_FINISH -
	F_FINISH -
}
DEL SSD469 <- glob.c16.start CLK glob.c16 delay 1
DEL F477 <- TS472 CLK glob.c16 delay 1
OR484[0] = TS460 OR F477
DEL S482 <- F477 CLK glob.c16 delay 1
DEL FF486 <- FS473 CLK glob.c16 delay 1
WHEN {
	T_START  TS472
	F_START  FS473
	FINISH   F471
	<-
	START    S470
	TEST     prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforeset[0]
	T_FINISH S482
	F_FINISH FF486
}
ILOOP  S470 <- SSD469 F471
MADDR498[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7] cast - pad
OP E505[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7] != 255	(unsigned, unsigned)
OP E509[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7] + 1	(unsigned, unsigned)
S512[0..7] = E509[0..8] cast - pad
WHEN {
	T_START  TS502
	F_START  FS503
	FINISH   -
	<-
	START    TS491
	TEST     E505[0]
	T_FINISH -
	F_FINISH -
}
WHEN {
	T_START  TS491
	F_START  FS492
	FINISH   -
	<-
	START    SDD923
	TEST     OR484[0]
	T_FINISH -
	F_FINISH -
}
OP E533[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E534[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E536[0] = FMOD7/uart_cpu.command[0..5] == 1	(unsigned, unsigned)
E537[0] = E536[0] AND E533[0] AND E534[0]
DEL F547 <- TS530 CLK glob.c16 delay 1
OP E559[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD571
	<-
	CLK       glob.c16
	START_IN  F547
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F566 <- SD571 CLK glob.c16 delay 1
OP E574[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.i[0..2] != 4	(unsigned, unsigned)
OP E578[0..31] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.data[0..31]	(unsigned, unsigned, unsigned)
S581[0..7] = E578[0..31] cast - pad
DEL F577 <- SD601 CLK glob.c16 delay 1
OP E587[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.i[0..2] + 1	(unsigned, unsigned)
S590[0..2] = E587[0..3] cast - pad
OP E594[0..31] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.data[0..31] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD601
	<-
	CLK       glob.c16
	START_IN  SB573
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB573
	FINISH     F604
	<-
	START      F566
	TEST       E574[0]
	CONTIN     F577
	C          glob.c16
	RESET      null
}
DEL S610 <- F604 CLK glob.c16 delay 1
DEL F614 <- S610 CLK glob.c16 delay 1
DEL FF616 <- FS531 CLK glob.c16 delay 1
WHEN {
	T_START  TS530
	F_START  FS531
	FINISH   F529
	<-
	START    S528
	TEST     E537[0]
	T_FINISH F614
	F_FINISH FF616
}
ILOOP  S528 <- SSD469 F529
OP E627[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E628[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E630[0] = FMOD7/uart_cpu.command[0..5] == 1	(unsigned, unsigned)
E631[0] = E630[0] AND E627[0] AND E628[0]
DEL S635 <- TS624 CLK glob.c16 delay 1
DEL F639 <- S635 CLK glob.c16 delay 1
WHEN {
	T_START  TS624
	F_START  FS625
	FINISH   F623
	<-
	START    S622
	TEST     E631[0]
	T_FINISH F639
	F_FINISH FF626
}
DEL FF626 <- FS625 CLK glob.c16 delay 1
ILOOP  S622 <- SSD469 F623
OP E658[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E659[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E661[0] = FMOD7/uart_cpu.command[0..5] == 2	(unsigned, unsigned)
DEL F666 <- TS655 CLK glob.c16 delay 1
OP E669[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.i[0..3] != 11	(unsigned, unsigned)
OP E674[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.data[0..87] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD682
	<-
	CLK       glob.c16
	START_IN  SB668
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F673 <- SD682 CLK glob.c16 delay 1
OP E686[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.i[0..3] + 1	(unsigned, unsigned)
S689[0..3] = E686[0..4] cast - pad
OP E695[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.data[0..87] >r> 8	(unsigned, unsigned)
DEL F694 <- F673 CLK glob.c16 delay 1
WHILE {
	START_B    SB668
	FINISH     F701
	<-
	START      F666
	TEST       E669[0]
	CONTIN     F694
	C          glob.c16
	RESET      null
}
E708[0..8,9..20,21,22..33,34,35..50,51..66,67..82] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.data[0..87] cast - pad
S711[0..8] = E708[0..8] cast - pad
S712[0..11] = E708[9..20] cast - sign_extend
S713[0] = E708[21] cast - pad
S714[0..11] = E708[22..33] cast - pad
S715[0] = E708[34] cast - pad
S716[0..15] = E708[35..50] cast - pad
S717[0..15] = E708[51..66] cast - pad
S718[0..15] = E708[67..82] cast - pad
DEL F707 <- F701 CLK glob.c16 delay 1
DEL S721 <- F707 CLK glob.c16 delay 1
DEL F725 <- S721 CLK glob.c16 delay 1
AND730[0] = FMOD7/uart_cpu.rx.NE AND E661[0] AND E658[0] AND E659[0]
WHEN {
	T_START  TS655
	F_START  FS656
	FINISH   F654
	<-
	START    S653
	TEST     AND730[0]
	T_FINISH F725
	F_FINISH FF657
}
DEL FF657 <- FS656 CLK glob.c16 delay 1
ILOOP  S653 <- SSD469 F654
prog.read_1.case_25.read_value_1.in_6[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_6[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_6[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_6[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_6[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_6[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_6[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_6[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_7[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_7[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_7[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_7[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_7[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_7[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_7[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_7[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_8[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_8[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_8[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_8[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_8[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_8[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_8[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_8[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_9[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_9[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_9[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_9[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_9[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_9[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_9[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_9[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_10[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_10[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_10[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_10[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_10[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_10[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_10[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_10[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
prog.read_1.case_25.read_value_1.in_11[0..8] = prog.read_1.case_25.read_value_1.in_1[0..8] cast - pad
prog.read_1.case_25.read_value_1.in_11[9..20] = prog.read_1.case_25.read_value_1.in_1[9..20] cast - sign_extend
prog.read_1.case_25.read_value_1.in_11[21] = prog.read_1.case_25.read_value_1.in_1[21] cast - pad
prog.read_1.case_25.read_value_1.in_11[22..33] = prog.read_1.case_25.read_value_1.in_1[22..33] cast - pad
prog.read_1.case_25.read_value_1.in_11[34] = prog.read_1.case_25.read_value_1.in_1[34] cast - pad
prog.read_1.case_25.read_value_1.in_11[35..50] = prog.read_1.case_25.read_value_1.in_1[35..50] cast - pad
prog.read_1.case_25.read_value_1.in_11[51..66] = prog.read_1.case_25.read_value_1.in_1[51..66] cast - pad
prog.read_1.case_25.read_value_1.in_11[67..82] = prog.read_1.case_25.read_value_1.in_1[67..82] cast - pad
S740[0..8] = prog.read_1.case_25.read_value_1.in_11[0..8] cast - pad
S741[0..11] = prog.read_1.case_25.read_value_1.in_11[9..20] cast - sign_extend
S742[0] = prog.read_1.case_25.read_value_1.in_11[21] cast - pad
S743[0..11] = prog.read_1.case_25.read_value_1.in_11[22..33] cast - pad
S744[0] = prog.read_1.case_25.read_value_1.in_11[34] cast - pad
S745[0..15] = prog.read_1.case_25.read_value_1.in_11[35..50] cast - pad
S746[0..15] = prog.read_1.case_25.read_value_1.in_11[51..66] cast - pad
S747[0..15] = prog.read_1.case_25.read_value_1.in_11[67..82] cast - pad
S748[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[0..8] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[0..8] = S748[0..8]
S749[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[9..20] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[9..20] = S749[0..11]
S750[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[21] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[21] = S750[0]
S751[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[22..33] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[22..33] = S751[0..11]
S752[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[34] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[34] = S752[0]
S753[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[35..50] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[35..50] = S753[0..15]
S754[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[51..66] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[51..66] = S754[0..15]
S755[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[67..82] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[67..82] = S755[0..15]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[0..8] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[9..20] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[9..20] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[21] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[21] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[22..33] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[22..33] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[34] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[34] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[35..50] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[35..50] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[51..66] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[51..66] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_2[67..82] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[67..82] cast - pad
OP E761[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E762[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E764[0] = FMOD7/uart_cpu.command[0..5] == 2	(unsigned, unsigned)
E765[0] = E764[0] AND E761[0] AND E762[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[0..8] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[9..20] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[9..20] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[21] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[21] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[22..33] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[22..33] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[34] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[34] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[35..50] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[35..50] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[51..66] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[51..66] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[67..82] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_1[67..82] cast - pad
E776[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data_in_3[0..8,9..20,21,22..33,34,35..50,51..66,67..82] cast - pad
DEL F775 <- TS758 CLK glob.c16 delay 1
OP E787[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD799
	<-
	CLK       glob.c16
	START_IN  F775
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F794 <- SD799 CLK glob.c16 delay 1
OP E802[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.i[0..3] != 11	(unsigned, unsigned)
OP E806[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data[0..87]	(unsigned, unsigned, unsigned)
S809[0..7] = E806[0..87] cast - pad
DEL F805 <- SD829 CLK glob.c16 delay 1
OP E815[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.i[0..3] + 1	(unsigned, unsigned)
S818[0..3] = E815[0..4] cast - pad
OP E822[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data[0..87] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD829
	<-
	CLK       glob.c16
	START_IN  SB801
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB801
	FINISH     F832
	<-
	START      F794
	TEST       E802[0]
	CONTIN     F805
	C          glob.c16
	RESET      null
}
DEL S838 <- F832 CLK glob.c16 delay 1
DEL F842 <- S838 CLK glob.c16 delay 1
DEL FF844 <- FS759 CLK glob.c16 delay 1
WHEN {
	T_START  TS758
	F_START  FS759
	FINISH   F757
	<-
	START    S756
	TEST     E765[0]
	T_FINISH F842
	F_FINISH FF844
}
ILOOP  S756 <- SSD469 F757
NCF "NET PORT_C10 IOSTANDARD=LVCMOS33;" port PORT_C10
NCF "NET %n TNM=C16;" port null
NCF "TIMESPEC TS_C16=PERIOD C16 62.5;" port null
NCF "NET PORT_A3 IOSTANDARD=LVCMOS33;" port PORT_A3
NCF "NET PORT_B3 IOSTANDARD=LVCMOS33;" port PORT_B3
REG
	OUT  prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforeset[0]
	<-
	CLK  glob.c16
	D    TS624
	CE   SDD920
	R    GND
    {0x0}
REG
	OUT  prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforead[0]
	<-
	CLK  null
	D    -
	CE   -
	R    GND
    {0x0}	make const!
prog.s.RES[0..8] = GND expand
prog.s.RES[9..20] = GND expand
prog.s.RES[21] = GND
prog.s.RES[22..33] = GND expand
prog.s.RES[34] = GND
prog.s.RES[35..50] = GND expand
prog.s.RES[51..66] = GND expand
prog.s.RES[67..82] = GND expand
prog.s.D[0..8] = S711[0..8]
prog.s.D[9..20] = S712[0..11]
prog.s.D[21] = S713[0]
prog.s.D[22..33] = S714[0..11]
prog.s.D[34] = S715[0]
prog.s.D[35..50] = S716[0..15]
prog.s.D[51..66] = S717[0..15]
prog.s.D[67..82] = S718[0..15]
prog.s.CE[0..8] = F701 expand
prog.s.CE[9..20] = F701 expand
prog.s.CE[21] = F701
prog.s.CE[22..33] = F701 expand
prog.s.CE[34] = F701
prog.s.CE[35..50] = F701 expand
prog.s.CE[51..66] = F701 expand
prog.s.CE[67..82] = F701 expand
REG
	OUT  prog.s[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
	<-
	CLK  glob.c16
	D    prog.s.D[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
	CE   prog.s.CE[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
	R    prog.s.RES[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
    {0x00038002400041ec001ff}
OR849 = S610 OR S838
REG
	OUT  FMOD7/uart_cpu.rval[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS375
	R    OR849
    {0x0}
OR851 = S721 OR S635
REG
	OUT  FMOD7/uart_cpu.wstat[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS383
	R    OR851
    {0x0}
OR853 = S610 OR S838
REG
	OUT  FMOD7/uart_cpu.rmem[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS391
	R    OR853
    {0x0}
OR855 = S721 OR S635
REG
	OUT  FMOD7/uart_cpu.wmem[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS399
	R    OR855
    {0x0}
IBUF  INPUT857[0] <- PORT_A3 loc=A3 id b366 IBUF
FMOD7/uart_cpu.rx.D[0..7] = E208[0..7]
QUEUEBUFFER  depth 16 {
	OUT      FMOD7/uart_cpu.rx[0..7]
	NE       FMOD7/uart_cpu.rx.NE
	NF       FMOD7/uart_cpu.rx.NF
	<-
	CLK      glob.c16
	DATA     FMOD7/uart_cpu.rx.D[0..7]
	PUSH     TS164
	POP      FMOD7/uart_cpu.rx.POP
	RESET    GND
}
FMOD7/uart_cpu.rx.POP = SD371 OR SD429 OR SD442 OR SD682
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l.D[0..10] = S30[0..10]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..10]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l.D[0..10]
	CE   SDD919
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.prev[0]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0]
	CE   SDD918
	R    GND
    {0x1}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data.D[0..7] = S129[0..7]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data.D[0..7]
	CE   F116
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount.D[0..3] = S136[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount.D[0..3]
	CE   F116
	R    TS72
    {0x8}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.ferr[0]
	<-
	CLK  glob.c16
	D    E159[0]
	CE   F152
	R    TS72
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.oflo[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   FS165
	R    TS72
    {0x0}
OR869 = TS72 OR F87 OR F100
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count.D[0..3] = S49[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count.D[0..3]
	CE   TS39
	R    OR869
    {0x9}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]
	<-
	CLK  glob.c16
	D    INPUT857[0]
	CE   TS39
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16_prev[0]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]
	CE   TS39
	R    GND
    {0x0}
OR877 = SD799 OR SD571 OR SD829 OR SD601
SELECT {
	OUT  FMOD7/uart_cpu.tx.D[0..7]
	<-
	SEL  SD571
	IN   4
	SEL  SD601
	IN   S581[0..7]
	SEL  SD799
	IN   11
	SEL  SD829
	IN   S809[0..7]
    unselected out 0x0
}
QUEUEBUFFER  depth 16 {
	OUT      FMOD7/uart_cpu.tx[0..7]
	NE       FMOD7/uart_cpu.tx.NE
	NF       FMOD7/uart_cpu.tx.NF
	<-
	CLK      glob.c16
	DATA     FMOD7/uart_cpu.tx.D[0..7]
	PUSH     OR877
	POP      SD280
	RESET    GND
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.OUTPUT0[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.tx_out_1[0]
OBUF PORT_B3 <- OUTPUTBIT878[0] loc=B3 id b402 OBUF
OUTPUTBIT878[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.OUTPUT0[0] cast - pad
OR879 = F340 OR F326
OR882 = F290 OR SD280
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.txd_.D[0]
	<-
	SEL  F290
	IN   E297[0]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.txd_[0]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.txd_.D[0]
	CE   OR882
	R    OR879
    {0x1}
OR883 = FS237 OR SD280
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.txbusy_[0]
	<-
	CLK  glob.c16
	D    GND
	CE   GND
	R    OR883
    {0x1}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.bitcount.D[0..3] = S312[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.bitcount[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.bitcount.D[0..3]
	CE   F302
	R    SD280
    {0x8}
OR888 = SD280 OR F296
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.data.D[0..7]
	<-
	SEL  F296
	IN   E303[0..7]
	SEL  SD280
	IN   E222[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_345.uart_tx_0.data.D[0..7]
	CE   OR888
	R    GND
    {0x0}
FMOD7/uart_cpu.command.RES[0..5] = GND expand
FMOD7/uart_cpu.command.RES[6..7] = GND expand
FMOD7/uart_cpu.command.D[0..5] = S364[0..5]
FMOD7/uart_cpu.command.D[6..7] = S365[0..1]
FMOD7/uart_cpu.command.CE[0..5] = SD371 expand
FMOD7/uart_cpu.command.CE[6..7] = SD371 expand
REG
	OUT  FMOD7/uart_cpu.command[0..5,6..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.command.D[0..5,6..7]
	CE   FMOD7/uart_cpu.command.CE[0..5,6..7]
	R    FMOD7/uart_cpu.command.RES[0..5,6..7]
    {0x0}
OR892 = SD442 OR SD429
SELECT {
	OUT  FMOD7/uart_cpu.memaddr.D[0..15]
	<-
	SEL  SD429
	IN   S424[0..15]
	SEL  SD442
	IN   E435[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.memaddr[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.memaddr.D[0..15]
	CE   OR892
	R    GND
    {0x0}
OR893 = S721 OR S610 OR S635 OR S838
REG
	OUT  FMOD7/uart_cpu.idle[0]
	<-
	CLK  glob.c16
	D    GND
	CE   F413
	R    OR893
    {0x1}
RRAM - 1 ports {
	OUT0	FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.im_0[0..31]
	<-
	CLK0	glob.c16
	ADDR0	MADDR498[0..7]
	DATA0	-
	RE0	TS491
	WE0	-
    initialised
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address.D[0..7] = S512[0..7]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address.D[0..7]
	CE   TS502
	R    TS472
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.in_r[0..31]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.im_0[0..31]
	CE   VCC
	R    GND
    {0x0}
OR901 = SD601 OR TS530
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.data.D[0..31]
	<-
	SEL  SD601
	IN   E594[0..31]
	SEL  TS530
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.in_r[0..31]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.data[0..31]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.data.D[0..31]
	CE   OR901
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.empty[0]
	<-
	CLK  glob.c16
	D    E559[0]
	CE   TS530
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.i.D[0..2] = S590[0..2]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.i[0..2]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_432.read_value_gen_0.read_2.i.D[0..2]
	CE   SD601
	R    TS530
    {0x0}
OR908 = F673 OR SD682
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.data.D[0..87]
	<-
	SEL  SD682
	IN   E674[0..87]
	SEL  F673
	IN   E695[0..87]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.data[0..87]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.data.D[0..87]
	CE   OR908
	R    TS655
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.i.D[0..3] = S689[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.i[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_434.write_static_gen_1.if_405.write_3.i.D[0..3]
	CE   SB668
	R    TS655
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[0..8] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[9..20] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[21] = GND
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[22..33] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[34] = GND
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[35..50] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[51..66] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[67..82] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[0..8] = S740[0..8]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[9..20] = S741[0..11]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[21] = S742[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[22..33] = S743[0..11]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[34] = S744[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[35..50] = S745[0..15]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[51..66] = S746[0..15]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[67..82] = S747[0..15]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[0..8] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[9..20] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[21] = VCC
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[22..33] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[34] = VCC
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[35..50] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[51..66] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[67..82] = VCC expand
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.D[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
	CE   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.CE[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
	R    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.in_r.RES[0..8,9..20,21,22..33,34,35..50,51..66,67..82]
    {0x0}
OR914 = SD829 OR TS758
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data.D[0..87]
	<-
	SEL  TS758
	IN   E776[0..87]
	SEL  SD829
	IN   E822[0..87]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data[0..87]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.data.D[0..87]
	CE   OR914
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.empty[0]
	<-
	CLK  glob.c16
	D    E787[0]
	CE   TS758
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.i.D[0..3] = S818[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.i[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_397.forvar_245.for_435.read_value_gen_1.read_3.i.D[0..3]
	CE   SD829
	R    TS758
    {0x0}
DFF FDRSE {
	OUT      SDD918
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD469
init = R
}
DFF FDRSE {
	OUT      SDD919
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD469
init = R
}
DFF FDRSE {
	OUT      SDD920
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD469
init = R
}
DFF FDRSE {
	OUT      SDD921
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD469
init = R
}
DFF FDRSE {
	OUT      SDD922
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD469
init = R
}
DFF FDRSE {
	OUT      SDD923
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD469
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



