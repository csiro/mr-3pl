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
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi
	date             = 2022-10-07 09:59:09 +1100
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/prog.rpt
	rptToFile        = true
	sigList          = false
	skipPostProc     = true
	sourceFile       = prog.3pl
	unassOut         = fatal
	version          = 11.7.0M (devel svn 10083:10129M, dun202)


3PL version 11.7.0M (devel svn 10083:10129M, dun202).
Source file /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/prog.3pl
command line options - rntfs
2022-10-07 09:59:09 +1100

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
	currentDirectory = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi
	date             = 2022-10-07 09:59:09 +1100
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
	netFile          = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/prog.net
	netlistDisplay   = false
	netlistFile      = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/prog.edn
	optimiseConnect  = true
	outputDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/
	parentDirectory  = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/
	part             = xc3s400aft256-4
	postProcessTool  = ise
	reportFile       = /Users/dun202/src/mine/3PL/test/11.6.0/AES/commi/prog.rpt
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
prog.read_1.case_25.read_value_1.in_1[0..15] = prog.r[0..15]
prog.read_2.case_28.read_value_2.in_1[0..34] = prog.rr[0..34]
prog.read_3.case_31.read_value_3.in_1[0..5] = prog.rrr[0..5]
prog.receive_event_0.in_1[0] = prog.rrra[0]
prog.receive_event_0.sigint_0.sig_1[0] = prog.receive_event_0.in_1[0]
DEL F11 <- TS5 CLK glob.c16 delay 1
prog.receive_event_0.sigint_0.par_0.seq_0.wait_while_0.v_1[0] = prog.receive_event_0.sigint_0.sig_1[0]
DEL FB18 <- SB17 CLK glob.c16 delay 1
WHILE {
	START_B    SB17
	FINISH     F21
	<-
	START      F11
	TEST       prog.receive_event_0.sigint_0.par_0.seq_0.wait_while_0.v_1[0]
	CONTIN     FB18
	C          glob.c16
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
DEL FF7 <- FS6 CLK glob.c16 delay 1
ILOOP  S3 <- SSD1823 F4
DFF FDRSE {
	OUT      W31[0]
	<-
	D        TS1294
	C        glob.c16
	CE       VCC
	R        GND
	S        GND
init = R
}
DEL F40 <- TS34 CLK glob.c16 delay 1
DEL FB47 <- SB46 CLK glob.c16 delay 1
WHILE {
	START_B    SB46
	FINISH     F50
	<-
	START      F40
	TEST       W31[0]
	CONTIN     FB47
	C          glob.c16
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
DEL FF36 <- FS35 CLK glob.c16 delay 1
ILOOP  S32 <- SSD1823 F33
S60[0..8] = prog.s[0..8] cast - pad
prog.read_value_4.in_1[0..8] = S60[0..8]
S61[0..11] = prog.s[9..20] cast - sign_extend
prog.read_value_4.in_1[9..20] = S61[0..11]
S62[0] = prog.s[21] cast - pad
prog.read_value_4.in_1[21] = S62[0]
S63[0..11] = prog.s[22..33] cast - pad
prog.read_value_4.in_1[22..33] = S63[0..11]
S64[0] = prog.s[34] cast - pad
prog.read_value_4.in_1[34] = S64[0]
S65[0..7] = prog.s[35..42] cast - sign_extend
prog.read_value_4.in_1[35..42] = S65[0..7]
S66[0..7] = prog.s[43..50] cast - sign_extend
prog.read_value_4.in_1[43..50] = S66[0..7]
S67[0..7] = prog.s[51..58] cast - sign_extend
prog.read_value_4.in_1[51..58] = S67[0..7]
S68[0..7] = prog.s[59..66] cast - sign_extend
prog.read_value_4.in_1[59..66] = S68[0..7]
S69[0..7] = prog.s[67..74] cast - sign_extend
prog.read_value_4.in_1[67..74] = S69[0..7]
S70[0..7] = prog.s[75..82] cast - sign_extend
prog.read_value_4.in_1[75..82] = S70[0..7]
prog.read_value_4.in_2[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_2[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_2[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_2[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_2[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_2[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_2[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_2[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_2[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_2[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_2[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_value_4.in_3[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_3[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_3[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_3[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_3[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_3[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_3[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_3[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_3[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_3[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_3[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_7.case_45.read_value_5.in_1[0..11] = prog.uf[0..11]
prog.read_8.case_47.read_value_6.in_1[0..19] = prog.f[0..19]
prog.read_9.case_49.read_value_7.in_1[0..20] = prog.fl[0..20]
OP E74[0..10] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..10] >> 10	(unsigned, unsigned)
E75[0] = E74[0..10] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0] = E75[0]
OP E82[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.prev[0]	(unsigned)
E83[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0] AND E82[0]
OP E87[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..10] + 109	(unsigned, unsigned)
S90[0..10] = E87[0..11] cast - pad
OP E96[0] = ~FMOD7/uart_cpu.rx.NF	(unsigned)
OP E106[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3] + 1	(unsigned, unsigned)
S109[0..3] = E106[0..4] cast - pad
WHEN {
	T_START  TS99
	F_START  FS100
	FINISH   -
	<-
	START    SDD3404
	TEST     E83[0]
	T_FINISH -
	F_FINISH -
}
OP E128[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3] == 0	(unsigned, unsigned)
E129[0] = E83[0] AND E128[0]
OP E135[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]	(unsigned)
E136[0] = E135[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16_prev[0]
DEL F140 <- TS132 CLK glob.c16 delay 1
OP E143[0] = ~E129[0]	(unsigned)
DEL FB144 <- SB142 CLK glob.c16 delay 1
WHILE {
	START_B    SB142
	FINISH     F147
	<-
	START      F140
	TEST       E143[0]
	CONTIN     FB144
	C          glob.c16
	RESET      null
}
DEL F153 <- F147 CLK glob.c16 delay 1
OP E156[0] = ~E129[0]	(unsigned)
DEL FB157 <- SB155 CLK glob.c16 delay 1
WHILE {
	START_B    SB155
	FINISH     F160
	<-
	START      F153
	TEST       E156[0]
	CONTIN     FB157
	C          glob.c16
	RESET      null
}
DEL F166 <- F160 CLK glob.c16 delay 1
OP E169[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E172[0] = ~E129[0]	(unsigned)
DEL FB173 <- SB171 CLK glob.c16 delay 1
WHILE {
	START_B    SB171
	FINISH     F176
	<-
	START      SB168
	TEST       E172[0]
	CONTIN     FB173
	C          glob.c16
	RESET      null
}
OP E184[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7] << 1	(unsigned, unsigned)
E185[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0] cast - pad
OP E186[0..8] = E184[0..8] | E185[0]	(unsigned, unsigned)
S189[0..7] = E186[0..8] cast - pad
DEL F183 <- F176 CLK glob.c16 delay 1
OP E193[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S196[0..3] = E193[0..4] cast - pad
WHILE {
	START_B    SB168
	FINISH     F202
	<-
	START      F166
	TEST       E169[0]
	CONTIN     F183
	C          glob.c16
	RESET      null
}
OP E208[0] = ~E129[0]	(unsigned)
DEL FB209 <- SB207 CLK glob.c16 delay 1
WHILE {
	START_B    SB207
	FINISH     F212
	<-
	START      F202
	TEST       E208[0]
	CONTIN     FB209
	C          glob.c16
	RESET      null
}
OP E219[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]	(unsigned)
DEL F218 <- F212 CLK glob.c16 delay 1
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.in_1[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7]
E234[0,1,2,3,4,5,6,7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.in_1[0..7] cast - pad
S235[0] = E234[0] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[0] = S235[0]
S236[0] = E234[1] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[1] = S236[0]
S237[0] = E234[2] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[2] = S237[0]
S238[0] = E234[3] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[3] = S238[0]
S239[0] = E234[4] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[4] = S239[0]
S240[0] = E234[5] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[5] = S240[0]
S241[0] = E234[6] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[6] = S241[0]
S242[0] = E234[7] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.i_1[7] = S242[0]
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
E267[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.seq_4.reverse_0.o_9[0,1,2,3,4,5,6,7] cast - pad
E268[0..7] = E267[0..7] cast - pad
DEL F223 <- F218 CLK glob.c16 delay 1
WHEN {
	T_START  TS224
	F_START  FS225
	FINISH   -
	<-
	START    F218
	TEST     FMOD7/uart_cpu.rx.NF
	T_FINISH -
	F_FINISH -
}
DEL FF279 <- FS133 CLK glob.c16 delay 1
WHEN {
	T_START  TS132
	F_START  FS133
	FINISH   F131
	<-
	START    S130
	TEST     E136[0]
	T_FINISH F223
	F_FINISH FF279
}
ILOOP  S130 <- SSD1823 F131
E282[0..7] = FMOD7/uart_cpu.tx[0..7] cast - pad
DEL E289[0] <- E289[0] CLK glob.c16 delay 16 EN E83[0] initialised
E290[0] = E289[0] AND E83[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.tx_out_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.txd_[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.tx_out_2[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.txd_[0]
OP E304[0] = ~E290[0]	(unsigned)
DEL FB307 <- SB306 CLK glob.c16 delay 1
WHILE {
	START_B    SB306
	FINISH     F310
	<-
	START      TS296
	TEST       E304[0]
	CONTIN     FB307
	C          glob.c16
	RESET      null
}
DEL F316 <- SD340 CLK glob.c16 delay 1
EXECP no priority, buffered queues only {
	START_DEL SD340
	<-
	CLK       glob.c16
	START_IN  F310
	BQAV      FMOD7/uart_cpu.tx.NE
}
OP E343[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.bitcount[0..3] != 0	(unsigned, unsigned)
OP E344[0] = ~E290[0]	(unsigned)
DEL FB347 <- SB346 CLK glob.c16 delay 1
WHILE {
	START_B    SB346
	FINISH     F350
	<-
	START      SB342
	TEST       E344[0]
	CONTIN     FB347
	C          glob.c16
	RESET      null
}
E357[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.data[0..7] cast - pad
DEL F356 <- F350 CLK glob.c16 delay 1
OP E363[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.data[0..7] >> 1	(unsigned, unsigned)
DEL F362 <- F356 CLK glob.c16 delay 1
OP E369[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.bitcount[0..3] - 1	(unsigned, unsigned)
S372[0..3] = E369[0..4] cast - pad
DEL F368 <- F362 CLK glob.c16 delay 1
WHILE {
	START_B    SB342
	FINISH     F376
	<-
	START      F316
	TEST       E343[0]
	CONTIN     F368
	C          glob.c16
	RESET      null
}
OP E380[0] = ~E290[0]	(unsigned)
DEL FB383 <- SB382 CLK glob.c16 delay 1
WHILE {
	START_B    SB382
	FINISH     F386
	<-
	START      F376
	TEST       E380[0]
	CONTIN     FB383
	C          glob.c16
	RESET      null
}
DEL F392 <- F386 CLK glob.c16 delay 1
OP E394[0] = ~E290[0]	(unsigned)
DEL FB397 <- SB396 CLK glob.c16 delay 1
WHILE {
	START_B    SB396
	FINISH     F400
	<-
	START      F392
	TEST       E394[0]
	CONTIN     FB397
	C          glob.c16
	RESET      null
}
DEL F406 <- F400 CLK glob.c16 delay 1
DEL F410 <- FS297 CLK glob.c16 delay 1
WHEN {
	T_START  TS296
	F_START  FS297
	FINISH   F295
	<-
	START    S294
	TEST     FMOD7/uart_cpu.tx.NE
	T_FINISH F406
	F_FINISH F410
}
ILOOP  S294 <- SSD1823 F295
E421[0..5,6..7] = FMOD7/uart_cpu.rx[0..7] cast - pad
S424[0..5] = E421[0..5] cast - pad
S425[0..1] = E421[6..7] cast - pad
EXECP no priority, buffered queues only {
	START_DEL SD431
	<-
	CLK       glob.c16
	START_IN  TS415
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F420 <- SD431 CLK glob.c16 delay 1
DEL F434 <- F420 CLK glob.c16 delay 1
OP DO441 = FMOD7/uart_cpu.command[6..7] == 0	(unsigned, unsigned)
CS435 = F420 AND DO441
OP DO449 = FMOD7/uart_cpu.command[6..7] == 2	(unsigned, unsigned)
CS443 = F420 AND DO449
OP DO457 = FMOD7/uart_cpu.command[6..7] == 1	(unsigned, unsigned)
CS451 = F420 AND DO457
OP DO465 = FMOD7/uart_cpu.command[6..7] == 3	(unsigned, unsigned)
CS459 = F420 AND DO465
E477[0] = FMOD7/uart_cpu.rmem[0] OR FMOD7/uart_cpu.wmem[0]
E481[0..7] = FMOD7/uart_cpu.rx[0..7] cast - pad
S484[0..15] = E481[0..7] cast - pad
EXECP no priority, buffered queues only {
	START_DEL SD489
	<-
	CLK       glob.c16
	START_IN  TS474
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F480 <- SD489 CLK glob.c16 delay 1
E493[0..7] = FMOD7/uart_cpu.rx[0..7] cast - pad
OP E494[0..15] = E493[0..7] << 8	(unsigned, unsigned)
OP E495[0..15] = FMOD7/uart_cpu.memaddr[0..15] | E494[0..15]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD502
	<-
	CLK       glob.c16
	START_IN  F480
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F492 <- SD502 CLK glob.c16 delay 1
DEL FF504 <- FS475 CLK glob.c16 delay 1
WHEN {
	T_START  TS474
	F_START  FS475
	FINISH   F473
	<-
	START    F434
	TEST     E477[0]
	T_FINISH F492
	F_FINISH FF504
}
DEL F508 <- F473 CLK glob.c16 delay 1
DEL FF512 <- FS416 CLK glob.c16 delay 1
WHEN {
	T_START  TS415
	F_START  FS416
	FINISH   F414
	<-
	START    S413
	TEST     FMOD7/uart_cpu.idle[0]
	T_FINISH F508
	F_FINISH FF512
}
ILOOP  S413 <- SSD1823 F414
prog.read_value_4.in_4[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_4[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_4[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_4[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_4[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_4[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_4[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_4[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_4[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_4[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_4[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_value_4.in_5[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_5[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_5[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_5[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_5[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_5[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_5[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_5[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_5[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_5[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_5[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
WHEN {
	T_START  TS520
	F_START  FS521
	FINISH   -
	<-
	START    SDD3399
	TEST     prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforead[0]
	T_FINISH -
	F_FINISH -
}
DEL F537 <- TS532 CLK glob.c16 delay 1
OR544[0] = TS520 OR F537
DEL S542 <- F537 CLK glob.c16 delay 1
DEL FF546 <- FS533 CLK glob.c16 delay 1
WHEN {
	T_START  TS532
	F_START  FS533
	FINISH   F531
	<-
	START    S530
	TEST     prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforeset[0]
	T_FINISH S542
	F_FINISH FF546
}
ILOOP  S530 <- SSD1823 F531
MADDR558[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..8] cast - pad
OP E565[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..8] != 511	(unsigned, unsigned)
OP E569[0..9] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..8] + 1	(unsigned, unsigned)
S572[0..8] = E569[0..9] cast - pad
WHEN {
	T_START  TS562
	F_START  FS563
	FINISH   -
	<-
	START    TS551
	TEST     E565[0]
	T_FINISH -
	F_FINISH -
}
WHEN {
	T_START  TS551
	F_START  FS552
	FINISH   -
	<-
	START    SDD3405
	TEST     OR544[0]
	T_FINISH -
	F_FINISH -
}
OP E593[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E594[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E596[0] = FMOD7/uart_cpu.command[0..5] == 1	(unsigned, unsigned)
E597[0] = E596[0] AND E593[0] AND E594[0]
DEL F607 <- TS590 CLK glob.c16 delay 1
OP E619[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD631
	<-
	CLK       glob.c16
	START_IN  F607
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F626 <- SD631 CLK glob.c16 delay 1
OP E634[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.i[0..2] != 4	(unsigned, unsigned)
OP E638[0..31] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.data[0..31]	(unsigned, unsigned, unsigned)
S641[0..7] = E638[0..31] cast - pad
DEL F637 <- SD661 CLK glob.c16 delay 1
OP E647[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.i[0..2] + 1	(unsigned, unsigned)
S650[0..2] = E647[0..3] cast - pad
OP E654[0..31] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.data[0..31] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD661
	<-
	CLK       glob.c16
	START_IN  SB633
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB633
	FINISH     F664
	<-
	START      F626
	TEST       E634[0]
	CONTIN     F637
	C          glob.c16
	RESET      null
}
DEL S670 <- F664 CLK glob.c16 delay 1
DEL F674 <- S670 CLK glob.c16 delay 1
DEL FF676 <- FS591 CLK glob.c16 delay 1
WHEN {
	T_START  TS590
	F_START  FS591
	FINISH   F589
	<-
	START    S588
	TEST     E597[0]
	T_FINISH F674
	F_FINISH FF676
}
ILOOP  S588 <- SSD1823 F589
OP E693[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E694[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E696[0] = FMOD7/uart_cpu.command[0..5] == 1	(unsigned, unsigned)
E697[0] = E696[0] AND E693[0] AND E694[0]
DEL S701 <- TS690 CLK glob.c16 delay 1
DEL F705 <- S701 CLK glob.c16 delay 1
WHEN {
	T_START  TS690
	F_START  FS691
	FINISH   F689
	<-
	START    S688
	TEST     E697[0]
	T_FINISH F705
	F_FINISH FF692
}
DEL FF692 <- FS691 CLK glob.c16 delay 1
ILOOP  S688 <- SSD1823 F689
OP E724[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E725[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E727[0] = FMOD7/uart_cpu.command[0..5] == 2	(unsigned, unsigned)
DEL F732 <- TS721 CLK glob.c16 delay 1
OP E735[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.i[0..1] != 2	(unsigned, unsigned)
OP E740[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.data[0..15] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD748
	<-
	CLK       glob.c16
	START_IN  SB734
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F739 <- SD748 CLK glob.c16 delay 1
OP E752[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.i[0..1] + 1	(unsigned, unsigned)
S755[0..1] = E752[0..2] cast - pad
OP E761[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.data[0..15] >r> 8	(unsigned, unsigned)
DEL F760 <- F739 CLK glob.c16 delay 1
WHILE {
	START_B    SB734
	FINISH     F767
	<-
	START      F732
	TEST       E735[0]
	CONTIN     F760
	C          glob.c16
	RESET      null
}
E774[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.data[0..15] cast - pad
DEL F773 <- F767 CLK glob.c16 delay 1
DEL S779 <- F773 CLK glob.c16 delay 1
DEL F783 <- S779 CLK glob.c16 delay 1
AND788[0] = FMOD7/uart_cpu.rx.NE AND E727[0] AND E724[0] AND E725[0]
WHEN {
	T_START  TS721
	F_START  FS722
	FINISH   F720
	<-
	START    S719
	TEST     AND788[0]
	T_FINISH F783
	F_FINISH FF723
}
DEL FF723 <- FS722 CLK glob.c16 delay 1
ILOOP  S719 <- SSD1823 F720
OP E803[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E804[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E806[0] = FMOD7/uart_cpu.command[0..5] == 2	(unsigned, unsigned)
E807[0] = E806[0] AND E803[0] AND E804[0]
DEL F817 <- TS800 CLK glob.c16 delay 1
OP E829[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD841
	<-
	CLK       glob.c16
	START_IN  F817
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F836 <- SD841 CLK glob.c16 delay 1
OP E844[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.i[0..1] != 2	(unsigned, unsigned)
OP E848[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.data[0..15]	(unsigned, unsigned, unsigned)
S851[0..7] = E848[0..15] cast - pad
DEL F847 <- SD871 CLK glob.c16 delay 1
OP E857[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.i[0..1] + 1	(unsigned, unsigned)
S860[0..1] = E857[0..2] cast - pad
OP E864[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD871
	<-
	CLK       glob.c16
	START_IN  SB843
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB843
	FINISH     F874
	<-
	START      F836
	TEST       E844[0]
	CONTIN     F847
	C          glob.c16
	RESET      null
}
DEL S880 <- F874 CLK glob.c16 delay 1
DEL F884 <- S880 CLK glob.c16 delay 1
DEL FF886 <- FS801 CLK glob.c16 delay 1
WHEN {
	T_START  TS800
	F_START  FS801
	FINISH   F799
	<-
	START    S798
	TEST     E807[0]
	T_FINISH F884
	F_FINISH FF886
}
ILOOP  S798 <- SSD1823 F799
OP E897[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E898[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E900[0] = FMOD7/uart_cpu.command[0..5] == 3	(unsigned, unsigned)
DEL F905 <- TS894 CLK glob.c16 delay 1
OP E908[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.i[0..2] != 5	(unsigned, unsigned)
OP E913[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.data[0..39] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD921
	<-
	CLK       glob.c16
	START_IN  SB907
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F912 <- SD921 CLK glob.c16 delay 1
OP E925[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.i[0..2] + 1	(unsigned, unsigned)
S928[0..2] = E925[0..3] cast - pad
OP E934[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.data[0..39] >r> 8	(unsigned, unsigned)
DEL F933 <- F912 CLK glob.c16 delay 1
WHILE {
	START_B    SB907
	FINISH     F940
	<-
	START      F905
	TEST       E908[0]
	CONTIN     F933
	C          glob.c16
	RESET      null
}
E947[0..34] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.data[0..39] cast - sign_extend
DEL F946 <- F940 CLK glob.c16 delay 1
DEL S952 <- F946 CLK glob.c16 delay 1
DEL F956 <- S952 CLK glob.c16 delay 1
AND961[0] = FMOD7/uart_cpu.rx.NE AND E900[0] AND E897[0] AND E898[0]
WHEN {
	T_START  TS894
	F_START  FS895
	FINISH   F893
	<-
	START    S892
	TEST     AND961[0]
	T_FINISH F956
	F_FINISH FF896
}
DEL FF896 <- FS895 CLK glob.c16 delay 1
ILOOP  S892 <- SSD1823 F893
OP E976[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E977[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E979[0] = FMOD7/uart_cpu.command[0..5] == 3	(unsigned, unsigned)
E980[0] = E979[0] AND E976[0] AND E977[0]
E991[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.in_r[0..34] cast - sign_extend
DEL F990 <- TS973 CLK glob.c16 delay 1
OP E1002[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1014
	<-
	CLK       glob.c16
	START_IN  F990
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F1009 <- SD1014 CLK glob.c16 delay 1
OP E1017[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.i[0..2] != 5	(unsigned, unsigned)
OP E1021[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.data[0..39]	(unsigned, unsigned, unsigned)
S1024[0..7] = E1021[0..39] cast - pad
DEL F1020 <- SD1044 CLK glob.c16 delay 1
OP E1030[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.i[0..2] + 1	(unsigned, unsigned)
S1033[0..2] = E1030[0..3] cast - pad
OP E1037[0..39] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.data[0..39] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1044
	<-
	CLK       glob.c16
	START_IN  SB1016
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1016
	FINISH     F1047
	<-
	START      F1009
	TEST       E1017[0]
	CONTIN     F1020
	C          glob.c16
	RESET      null
}
DEL S1053 <- F1047 CLK glob.c16 delay 1
DEL F1057 <- S1053 CLK glob.c16 delay 1
DEL FF1059 <- FS974 CLK glob.c16 delay 1
WHEN {
	T_START  TS973
	F_START  FS974
	FINISH   F972
	<-
	START    S971
	TEST     E980[0]
	T_FINISH F1057
	F_FINISH FF1059
}
ILOOP  S971 <- SSD1823 F972
OP E1070[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1071[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E1073[0] = FMOD7/uart_cpu.command[0..5] == 4	(unsigned, unsigned)
DEL F1078 <- TS1067 CLK glob.c16 delay 1
OP E1081[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.i[0] != 1	(unsigned, unsigned)
OP E1086[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.data[0..7] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1094
	<-
	CLK       glob.c16
	START_IN  SB1080
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F1085 <- SD1094 CLK glob.c16 delay 1
OP E1098[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.i[0] + 1	(unsigned, unsigned)
S1101[0] = E1098[0..1] cast - pad
OP E1107[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.data[0..7] >r> 8	(unsigned, unsigned)
DEL F1106 <- F1085 CLK glob.c16 delay 1
WHILE {
	START_B    SB1080
	FINISH     F1113
	<-
	START      F1078
	TEST       E1081[0]
	CONTIN     F1106
	C          glob.c16
	RESET      null
}
E1120[0..5] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.data[0..7] cast - pad
DEL F1119 <- F1113 CLK glob.c16 delay 1
DEL S1125 <- F1119 CLK glob.c16 delay 1
DEL F1129 <- S1125 CLK glob.c16 delay 1
AND1134[0] = FMOD7/uart_cpu.rx.NE AND E1073[0] AND E1070[0] AND E1071[0]
WHEN {
	T_START  TS1067
	F_START  FS1068
	FINISH   F1066
	<-
	START    S1065
	TEST     AND1134[0]
	T_FINISH F1129
	F_FINISH FF1069
}
DEL FF1069 <- FS1068 CLK glob.c16 delay 1
ILOOP  S1065 <- SSD1823 F1066
OP E1155[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1156[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E1158[0] = FMOD7/uart_cpu.command[0..5] == 4	(unsigned, unsigned)
E1159[0] = E1158[0] AND E1155[0] AND E1156[0]
E1170[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.in_r[0..5] cast - pad
DEL F1169 <- TS1152 CLK glob.c16 delay 1
OP E1181[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1193
	<-
	CLK       glob.c16
	START_IN  F1169
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F1188 <- SD1193 CLK glob.c16 delay 1
OP E1196[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.i[0] != 1	(unsigned, unsigned)
OP E1200[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.data[0..7]	(unsigned, unsigned, unsigned)
DEL F1199 <- SD1222 CLK glob.c16 delay 1
OP E1208[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.i[0] + 1	(unsigned, unsigned)
S1211[0] = E1208[0..1] cast - pad
OP E1215[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.data[0..7] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1222
	<-
	CLK       glob.c16
	START_IN  SB1195
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1195
	FINISH     F1225
	<-
	START      F1188
	TEST       E1196[0]
	CONTIN     F1199
	C          glob.c16
	RESET      null
}
DEL S1231 <- F1225 CLK glob.c16 delay 1
DEL F1235 <- S1231 CLK glob.c16 delay 1
DEL FF1237 <- FS1153 CLK glob.c16 delay 1
WHEN {
	T_START  TS1152
	F_START  FS1153
	FINISH   F1151
	<-
	START    S1150
	TEST     E1159[0]
	T_FINISH F1235
	F_FINISH FF1237
}
ILOOP  S1150 <- SSD1823 F1151
OP E1245[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1246[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E1248[0] = FMOD7/uart_cpu.command[0..5] == 5	(unsigned, unsigned)
DEL F1253 <- TS1242 CLK glob.c16 delay 1
OP E1256[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.i[0..1] != 2	(unsigned, unsigned)
OP E1261[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.data[0..15] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1269
	<-
	CLK       glob.c16
	START_IN  SB1255
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F1260 <- SD1269 CLK glob.c16 delay 1
OP E1273[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.i[0..1] + 1	(unsigned, unsigned)
S1276[0..1] = E1273[0..2] cast - pad
OP E1282[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.data[0..15] >r> 8	(unsigned, unsigned)
DEL F1281 <- F1260 CLK glob.c16 delay 1
WHILE {
	START_B    SB1255
	FINISH     F1288
	<-
	START      F1253
	TEST       E1256[0]
	CONTIN     F1281
	C          glob.c16
	RESET      null
}
E1301[0..13] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.data[0..15] cast - sign_extend
DEL F1293 <- F1288 CLK glob.c16 delay 1
WHEN {
	T_START  TS1294
	F_START  FS1295
	FINISH   -
	<-
	START    F1288
	TEST     prog.q.NF
	T_FINISH -
	F_FINISH -
}
DEL S1309 <- F1293 CLK glob.c16 delay 1
DEL F1313 <- S1309 CLK glob.c16 delay 1
AND1318[0] = E1248[0] AND E1245[0] AND E1246[0] AND prog.q.NF AND FMOD7/uart_cpu.rx.NE
WHEN {
	T_START  TS1242
	F_START  FS1243
	FINISH   F1241
	<-
	START    S1240
	TEST     AND1318[0]
	T_FINISH F1313
	F_FINISH FF1244
}
DEL FF1244 <- FS1243 CLK glob.c16 delay 1
ILOOP  S1240 <- SSD1823 F1241
OP E1325[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1326[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E1328[0] = FMOD7/uart_cpu.command[0..5] == 5	(unsigned, unsigned)
E1329[0] = E1328[0] AND E1325[0] AND E1326[0]
E1340[0..7] = prog.q.SPC[0..4] cast - pad
DEL F1339 <- TS1322 CLK glob.c16 delay 1
OP E1351[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1363
	<-
	CLK       glob.c16
	START_IN  F1339
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F1358 <- SD1363 CLK glob.c16 delay 1
OP E1366[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.i[0] != 1	(unsigned, unsigned)
OP E1370[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.data[0..7]	(unsigned, unsigned, unsigned)
DEL F1369 <- SD1392 CLK glob.c16 delay 1
OP E1378[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.i[0] + 1	(unsigned, unsigned)
S1381[0] = E1378[0..1] cast - pad
OP E1385[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.data[0..7] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1392
	<-
	CLK       glob.c16
	START_IN  SB1365
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1365
	FINISH     F1395
	<-
	START      F1358
	TEST       E1366[0]
	CONTIN     F1369
	C          glob.c16
	RESET      null
}
DEL S1401 <- F1395 CLK glob.c16 delay 1
DEL F1405 <- S1401 CLK glob.c16 delay 1
DEL FF1407 <- FS1323 CLK glob.c16 delay 1
WHEN {
	T_START  TS1322
	F_START  FS1323
	FINISH   F1321
	<-
	START    S1320
	TEST     E1329[0]
	T_FINISH F1405
	F_FINISH FF1407
}
ILOOP  S1320 <- SSD1823 F1321
OP E1415[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1416[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E1418[0] = FMOD7/uart_cpu.command[0..5] == 6	(unsigned, unsigned)
DEL F1423 <- TS1412 CLK glob.c16 delay 1
OP E1426[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.i[0] != 1	(unsigned, unsigned)
OP E1431[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.data[0..7] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1438
	<-
	CLK       glob.c16
	START_IN  SB1425
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F1430 <- SD1438 CLK glob.c16 delay 1
OP E1442[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.i[0] + 1	(unsigned, unsigned)
S1445[0] = E1442[0..1] cast - pad
OP E1451[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.data[0..7] >r> 8	(unsigned, unsigned)
DEL F1450 <- F1430 CLK glob.c16 delay 1
WHILE {
	START_B    SB1425
	FINISH     F1457
	<-
	START      F1423
	TEST       E1426[0]
	CONTIN     F1450
	C          glob.c16
	RESET      null
}
E1464[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.data[0..7] cast - pad
DEL F1463 <- F1457 CLK glob.c16 delay 1
DEL S1469 <- F1463 CLK glob.c16 delay 1
DEL F1473 <- S1469 CLK glob.c16 delay 1
AND1478[0] = FMOD7/uart_cpu.rx.NE AND E1418[0] AND E1415[0] AND E1416[0]
WHEN {
	T_START  TS1412
	F_START  FS1413
	FINISH   F1411
	<-
	START    S1410
	TEST     AND1478[0]
	T_FINISH F1473
	F_FINISH FF1414
}
DEL FF1414 <- FS1413 CLK glob.c16 delay 1
OP E1479[0] = prog.q.SPC[0..4] >= FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.ilevel[0..3]	(unsigned, unsigned)
DEL F1488 <- TS1482 CLK glob.c16 delay 1
DEL FB1495 <- SB1494 CLK glob.c16 delay 1
WHILE {
	START_B    SB1494
	FINISH     F1498
	<-
	START      F1488
	TEST       E1479[0]
	CONTIN     FB1495
	C          glob.c16
	RESET      null
}
AND1506[0] = E1479[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.sigint_2.q.NF
WHEN {
	T_START  TS1482
	F_START  FS1483
	FINISH   F1481
	<-
	START    S1480
	TEST     AND1506[0]
	T_FINISH F1498
	F_FINISH FF1484
}
DEL FF1484 <- FS1483 CLK glob.c16 delay 1
ILOOP  S1480 <- SSD1823 F1481
ILOOP  S1410 <- SSD1823 F1411
OP E1517[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1518[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E1520[0] = FMOD7/uart_cpu.command[0..5] == 6	(unsigned, unsigned)
E1521[0] = E1520[0] AND E1517[0] AND E1518[0]
E1532[0..15] = prog.q[0..13] cast - sign_extend
EXECP no priority, buffered queues only {
	START_DEL SD1539
	<-
	CLK       glob.c16
	START_IN  TS1525
	BQAV      prog.q.NE
}
DEL F1531 <- SD1539 CLK glob.c16 delay 1
AND1554 = prog.qo.NF AND prog.q.NE
EXECP no priority, buffered queues only {
	START_DEL SD1555
	<-
	CLK       glob.c16
	START_IN  TS1542
	BQAV      AND1554
}
DEL F1548 <- SD1555 CLK glob.c16 delay 1
DEL FF1556 <- FS1543 CLK glob.c16 delay 1
WHEN {
	T_START  TS1542
	F_START  FS1543
	FINISH   F1541
	<-
	START    TS1525
	TEST     prog.qo.NF
	T_FINISH F1548
	F_FINISH FF1556
}
WAIT {
    in:
        glob.c16
        null
        F1531
        F1541
    out:
        F1559
}
DEL FF1560 <- FS1526 CLK glob.c16 delay 1
WHEN {
	T_START  TS1525
	F_START  FS1526
	FINISH   F1524
	<-
	START    TS1514
	TEST     prog.q.NE
	T_FINISH F1559
	F_FINISH FF1560
}
DEL F1564 <- TS1514 CLK glob.c16 delay 1
OP E1568[0] = ~prog.q.NE	(unsigned)
WAIT {
    in:
        glob.c16
        null
        F1524
        F1564
    out:
        F1572
}
EXECP no priority, buffered queues only {
	START_DEL SD1580
	<-
	CLK       glob.c16
	START_IN  F1572
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F1575 <- SD1580 CLK glob.c16 delay 1
OP E1583[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.i[0..1] != 2	(unsigned, unsigned)
OP E1587[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.data[0..15]	(unsigned, unsigned, unsigned)
S1590[0..7] = E1587[0..15] cast - pad
DEL F1586 <- SD1610 CLK glob.c16 delay 1
OP E1596[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.i[0..1] + 1	(unsigned, unsigned)
S1599[0..1] = E1596[0..2] cast - pad
OP E1603[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1610
	<-
	CLK       glob.c16
	START_IN  SB1582
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1582
	FINISH     F1613
	<-
	START      F1575
	TEST       E1583[0]
	CONTIN     F1586
	C          glob.c16
	RESET      null
}
DEL S1619 <- F1613 CLK glob.c16 delay 1
DEL F1623 <- S1619 CLK glob.c16 delay 1
DEL FF1625 <- FS1515 CLK glob.c16 delay 1
WHEN {
	T_START  TS1514
	F_START  FS1515
	FINISH   F1513
	<-
	START    S1512
	TEST     E1521[0]
	T_FINISH F1623
	F_FINISH FF1625
}
ILOOP  S1512 <- SSD1823 F1513
OP E1639[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1640[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E1642[0] = FMOD7/uart_cpu.command[0..5] == 7	(unsigned, unsigned)
E1643[0] = E1642[0] AND E1639[0] AND E1640[0]
E1654[0..7] = prog.q.CNT[0..4] cast - pad
DEL F1653 <- TS1636 CLK glob.c16 delay 1
OP E1665[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1677
	<-
	CLK       glob.c16
	START_IN  F1653
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F1672 <- SD1677 CLK glob.c16 delay 1
OP E1680[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.i[0] != 1	(unsigned, unsigned)
OP E1684[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.data[0..7]	(unsigned, unsigned, unsigned)
DEL F1683 <- SD1706 CLK glob.c16 delay 1
OP E1692[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.i[0] + 1	(unsigned, unsigned)
S1695[0] = E1692[0..1] cast - pad
OP E1699[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.data[0..7] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1706
	<-
	CLK       glob.c16
	START_IN  SB1679
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1679
	FINISH     F1709
	<-
	START      F1672
	TEST       E1680[0]
	CONTIN     F1683
	C          glob.c16
	RESET      null
}
DEL S1715 <- F1709 CLK glob.c16 delay 1
DEL F1719 <- S1715 CLK glob.c16 delay 1
DEL FF1721 <- FS1637 CLK glob.c16 delay 1
WHEN {
	T_START  TS1636
	F_START  FS1637
	FINISH   F1635
	<-
	START    S1634
	TEST     E1643[0]
	T_FINISH F1719
	F_FINISH FF1721
}
ILOOP  S1634 <- SSD1823 F1635
OP E1729[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1730[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E1732[0] = FMOD7/uart_cpu.command[0..5] == 7	(unsigned, unsigned)
DEL F1737 <- TS1726 CLK glob.c16 delay 1
OP E1740[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.i[0] != 1	(unsigned, unsigned)
OP E1745[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.data[0..7] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1753
	<-
	CLK       glob.c16
	START_IN  SB1739
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F1744 <- SD1753 CLK glob.c16 delay 1
OP E1757[0..1] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.i[0] + 1	(unsigned, unsigned)
S1760[0] = E1757[0..1] cast - pad
OP E1766[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.data[0..7] >r> 8	(unsigned, unsigned)
DEL F1765 <- F1744 CLK glob.c16 delay 1
WHILE {
	START_B    SB1739
	FINISH     F1772
	<-
	START      F1737
	TEST       E1740[0]
	CONTIN     F1765
	C          glob.c16
	RESET      null
}
E1779[0..3] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.data[0..7] cast - pad
DEL F1778 <- F1772 CLK glob.c16 delay 1
DEL S1784 <- F1778 CLK glob.c16 delay 1
DEL F1788 <- S1784 CLK glob.c16 delay 1
AND1793[0] = FMOD7/uart_cpu.rx.NE AND E1732[0] AND E1729[0] AND E1730[0]
WHEN {
	T_START  TS1726
	F_START  FS1727
	FINISH   F1725
	<-
	START    S1724
	TEST     AND1793[0]
	T_FINISH F1788
	F_FINISH FF1728
}
DEL FF1728 <- FS1727 CLK glob.c16 delay 1
OP E1794[0] = prog.q.CNT[0..4] >= FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.ilevel[0..3]	(unsigned, unsigned)
DEL F1803 <- TS1797 CLK glob.c16 delay 1
DEL FB1810 <- SB1809 CLK glob.c16 delay 1
WHILE {
	START_B    SB1809
	FINISH     F1813
	<-
	START      F1803
	TEST       E1794[0]
	CONTIN     FB1810
	C          glob.c16
	RESET      null
}
AND1821[0] = E1794[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.sigint_3.q.NF
WHEN {
	T_START  TS1797
	F_START  FS1798
	FINISH   F1796
	<-
	START    S1795
	TEST     AND1821[0]
	T_FINISH F1813
	F_FINISH FF1799
}
DEL FF1799 <- FS1798 CLK glob.c16 delay 1
ILOOP  S1795 <- SSD1823 F1796
DEL SSD1823 <- glob.c16.start CLK glob.c16 delay 1
ILOOP  S1724 <- SSD1823 F1725
OP E1838[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1839[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E1841[0] = FMOD7/uart_cpu.command[0..5] == 8	(unsigned, unsigned)
E1842[0] = E1841[0] AND E1838[0] AND E1839[0]
E1853[0..15] = prog.qo[0..13] cast - sign_extend
EXECP no priority, buffered queues only {
	START_DEL SD1860
	<-
	CLK       glob.c16
	START_IN  TS1846
	BQAV      prog.qo.NE
}
DEL F1852 <- SD1860 CLK glob.c16 delay 1
DEL FF1863 <- FS1847 CLK glob.c16 delay 1
WHEN {
	T_START  TS1846
	F_START  FS1847
	FINISH   F1845
	<-
	START    TS1835
	TEST     prog.qo.NE
	T_FINISH F1852
	F_FINISH FF1863
}
DEL F1867 <- TS1835 CLK glob.c16 delay 1
OP E1871[0] = ~prog.qo.NE	(unsigned)
WAIT {
    in:
        glob.c16
        null
        F1845
        F1867
    out:
        F1875
}
EXECP no priority, buffered queues only {
	START_DEL SD1883
	<-
	CLK       glob.c16
	START_IN  F1875
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F1878 <- SD1883 CLK glob.c16 delay 1
OP E1886[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.i[0..1] != 2	(unsigned, unsigned)
OP E1890[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.data[0..15]	(unsigned, unsigned, unsigned)
S1893[0..7] = E1890[0..15] cast - pad
DEL F1889 <- SD1913 CLK glob.c16 delay 1
OP E1899[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.i[0..1] + 1	(unsigned, unsigned)
S1902[0..1] = E1899[0..2] cast - pad
OP E1906[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1913
	<-
	CLK       glob.c16
	START_IN  SB1885
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB1885
	FINISH     F1916
	<-
	START      F1878
	TEST       E1886[0]
	CONTIN     F1889
	C          glob.c16
	RESET      null
}
DEL S1922 <- F1916 CLK glob.c16 delay 1
DEL F1926 <- S1922 CLK glob.c16 delay 1
DEL FF1928 <- FS1836 CLK glob.c16 delay 1
WHEN {
	T_START  TS1835
	F_START  FS1836
	FINISH   F1834
	<-
	START    S1833
	TEST     E1842[0]
	T_FINISH F1926
	F_FINISH FF1928
}
ILOOP  S1833 <- SSD1823 F1834
OP E1939[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E1940[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E1942[0] = FMOD7/uart_cpu.command[0..5] == 8	(unsigned, unsigned)
DEL F1947 <- TS1936 CLK glob.c16 delay 1
OP E1950[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.i[0..1] != 2	(unsigned, unsigned)
OP E1955[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.data[0..15] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD1963
	<-
	CLK       glob.c16
	START_IN  SB1949
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F1954 <- SD1963 CLK glob.c16 delay 1
OP E1967[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.i[0..1] + 1	(unsigned, unsigned)
S1970[0..1] = E1967[0..2] cast - pad
OP E1976[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.data[0..15] >r> 8	(unsigned, unsigned)
DEL F1975 <- F1954 CLK glob.c16 delay 1
WHILE {
	START_B    SB1949
	FINISH     F1982
	<-
	START      F1947
	TEST       E1950[0]
	CONTIN     F1975
	C          glob.c16
	RESET      null
}
E1989[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.data[0..15] cast - pad
DEL F1988 <- F1982 CLK glob.c16 delay 1
DEL S1994 <- F1988 CLK glob.c16 delay 1
DEL F1998 <- S1994 CLK glob.c16 delay 1
AND2003[0] = FMOD7/uart_cpu.rx.NE AND E1942[0] AND E1939[0] AND E1940[0]
WHEN {
	T_START  TS1936
	F_START  FS1937
	FINISH   F1935
	<-
	START    S1934
	TEST     AND2003[0]
	T_FINISH F1998
	F_FINISH FF1938
}
DEL FF1938 <- FS1937 CLK glob.c16 delay 1
ILOOP  S1934 <- SSD1823 F1935
E2013[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.mdata[0..15] cast - pad
S2015[0..7] = FMOD7/uart_cpu.memaddr[0..15] cast - pad
MADDR2014[0..7] = S2015[0..7] cast - pad
MDATA2016[0..15] = E2013[0..15] cast - pad
WHEN {
	T_START  TS2007
	F_START  FS2008
	FINISH   -
	<-
	START    SDD3400
	TEST     F1988
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.data_in_1[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.data[0..15]
OP E2029[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2030[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E2032[0] = FMOD7/uart_cpu.command[0..5] == 9	(unsigned, unsigned)
E2033[0] = E2032[0] AND E2029[0] AND E2030[0]
DEL S2036 <- TS2026 CLK glob.c16 delay 1
OP E2040[0] = ~F2141	(unsigned)
DEL FB2041 <- SB2039 CLK glob.c16 delay 1
WHILE {
	START_B    SB2039
	FINISH     F2044
	<-
	START      S2036
	TEST       E2040[0]
	CONTIN     FB2041
	C          glob.c16
	RESET      null
}
E2058[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.data_in_1[0..15] cast - pad
DEL F2057 <- F2044 CLK glob.c16 delay 1
OP E2069[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2081
	<-
	CLK       glob.c16
	START_IN  F2057
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F2076 <- SD2081 CLK glob.c16 delay 1
OP E2084[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.i[0..1] != 2	(unsigned, unsigned)
OP E2088[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.data[0..15]	(unsigned, unsigned, unsigned)
S2091[0..7] = E2088[0..15] cast - pad
DEL F2087 <- SD2111 CLK glob.c16 delay 1
OP E2097[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.i[0..1] + 1	(unsigned, unsigned)
S2100[0..1] = E2097[0..2] cast - pad
OP E2104[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2111
	<-
	CLK       glob.c16
	START_IN  SB2083
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB2083
	FINISH     F2114
	<-
	START      F2076
	TEST       E2084[0]
	CONTIN     F2087
	C          glob.c16
	RESET      null
}
DEL S2120 <- F2114 CLK glob.c16 delay 1
DEL F2124 <- S2120 CLK glob.c16 delay 1
DEL FF2126 <- FS2027 CLK glob.c16 delay 1
WHEN {
	T_START  TS2026
	F_START  FS2027
	FINISH   F2025
	<-
	START    S2024
	TEST     E2033[0]
	T_FINISH F2124
	F_FINISH FF2126
}
S2137[0..7] = FMOD7/uart_cpu.memaddr[0..15] cast - pad
MADDR2136[0..7] = S2137[0..7] cast - pad
DEL S2135 <- TS2130 CLK glob.c16 delay 1
DEL F2141 <- S2135 CLK glob.c16 delay 1
DEL S2147 <- F2141 CLK glob.c16 delay 1
DEL FF2150 <- FS2131 CLK glob.c16 delay 1
WHEN {
	T_START  TS2130
	F_START  FS2131
	FINISH   F2129
	<-
	START    S2128
	TEST     TS2026
	T_FINISH S2147
	F_FINISH FF2150
}
ILOOP  S2024 <- SSD1823 F2025
ILOOP  S2128 <- SSD1823 F2129
OP E2162[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2163[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E2165[0] = FMOD7/uart_cpu.command[0..5] == 9	(unsigned, unsigned)
DEL F2170 <- TS2159 CLK glob.c16 delay 1
OP E2173[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.i[0..3] != 11	(unsigned, unsigned)
OP E2178[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.data[0..87] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2186
	<-
	CLK       glob.c16
	START_IN  SB2172
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F2177 <- SD2186 CLK glob.c16 delay 1
OP E2190[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.i[0..3] + 1	(unsigned, unsigned)
S2193[0..3] = E2190[0..4] cast - pad
OP E2199[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.data[0..87] >r> 8	(unsigned, unsigned)
DEL F2198 <- F2177 CLK glob.c16 delay 1
WHILE {
	START_B    SB2172
	FINISH     F2205
	<-
	START      F2170
	TEST       E2173[0]
	CONTIN     F2198
	C          glob.c16
	RESET      null
}
E2212[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.data[0..87] cast - pad
S2215[0..8] = E2212[0..8] cast - pad
S2216[0..11] = E2212[9..20] cast - sign_extend
S2217[0] = E2212[21] cast - pad
S2218[0..11] = E2212[22..33] cast - pad
S2219[0] = E2212[34] cast - pad
S2220[0..7] = E2212[35..42] cast - sign_extend
S2221[0..7] = E2212[43..50] cast - sign_extend
S2222[0..7] = E2212[51..58] cast - sign_extend
S2223[0..7] = E2212[59..66] cast - sign_extend
S2224[0..7] = E2212[67..74] cast - sign_extend
S2225[0..7] = E2212[75..82] cast - sign_extend
DEL F2211 <- F2205 CLK glob.c16 delay 1
DEL S2228 <- F2211 CLK glob.c16 delay 1
DEL F2232 <- S2228 CLK glob.c16 delay 1
AND2237[0] = FMOD7/uart_cpu.rx.NE AND E2165[0] AND E2162[0] AND E2163[0]
WHEN {
	T_START  TS2159
	F_START  FS2160
	FINISH   F2158
	<-
	START    S2157
	TEST     AND2237[0]
	T_FINISH F2232
	F_FINISH FF2161
}
DEL FF2161 <- FS2160 CLK glob.c16 delay 1
ILOOP  S2157 <- SSD1823 F2158
prog.read_value_4.in_6[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_6[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_6[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_6[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_6[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_6[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_6[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_6[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_6[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_6[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_6[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_value_4.in_7[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_7[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_7[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_7[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_7[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_7[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_7[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_7[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_7[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_7[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_7[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_value_4.in_8[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_8[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_8[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_8[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_8[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_8[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_8[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_8[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_8[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_8[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_8[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_value_4.in_9[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_9[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_9[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_9[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_9[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_9[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_9[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_9[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_9[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_9[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_9[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_value_4.in_10[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_10[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_10[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_10[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_10[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_10[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_10[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_10[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_10[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_10[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_10[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
prog.read_value_4.in_11[0..8] = prog.read_value_4.in_1[0..8] cast - pad
prog.read_value_4.in_11[9..20] = prog.read_value_4.in_1[9..20] cast - sign_extend
prog.read_value_4.in_11[21] = prog.read_value_4.in_1[21] cast - pad
prog.read_value_4.in_11[22..33] = prog.read_value_4.in_1[22..33] cast - pad
prog.read_value_4.in_11[34] = prog.read_value_4.in_1[34] cast - pad
prog.read_value_4.in_11[35..42] = prog.read_value_4.in_1[35..42] cast - sign_extend
prog.read_value_4.in_11[43..50] = prog.read_value_4.in_1[43..50] cast - sign_extend
prog.read_value_4.in_11[51..58] = prog.read_value_4.in_1[51..58] cast - sign_extend
prog.read_value_4.in_11[59..66] = prog.read_value_4.in_1[59..66] cast - sign_extend
prog.read_value_4.in_11[67..74] = prog.read_value_4.in_1[67..74] cast - sign_extend
prog.read_value_4.in_11[75..82] = prog.read_value_4.in_1[75..82] cast - sign_extend
S2247[0..8] = prog.read_value_4.in_11[0..8] cast - pad
S2248[0..11] = prog.read_value_4.in_11[9..20] cast - sign_extend
S2249[0] = prog.read_value_4.in_11[21] cast - pad
S2250[0..11] = prog.read_value_4.in_11[22..33] cast - pad
S2251[0] = prog.read_value_4.in_11[34] cast - pad
S2252[0..7] = prog.read_value_4.in_11[35..42] cast - sign_extend
S2253[0..7] = prog.read_value_4.in_11[43..50] cast - sign_extend
S2254[0..7] = prog.read_value_4.in_11[51..58] cast - sign_extend
S2255[0..7] = prog.read_value_4.in_11[59..66] cast - sign_extend
S2256[0..7] = prog.read_value_4.in_11[67..74] cast - sign_extend
S2257[0..7] = prog.read_value_4.in_11[75..82] cast - sign_extend
S2258[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[0..8] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[0..8] = S2258[0..8]
S2259[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[9..20] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[9..20] = S2259[0..11]
S2260[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[21] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[21] = S2260[0]
S2261[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[22..33] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[22..33] = S2261[0..11]
S2262[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[34] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[34] = S2262[0]
S2263[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[35..42] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[35..42] = S2263[0..7]
S2264[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[43..50] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[43..50] = S2264[0..7]
S2265[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[51..58] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[51..58] = S2265[0..7]
S2266[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[59..66] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[59..66] = S2266[0..7]
S2267[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[67..74] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[67..74] = S2267[0..7]
S2268[0..7] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[75..82] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[75..82] = S2268[0..7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[0..8] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[9..20] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[9..20] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[21] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[21] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[22..33] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[22..33] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[34] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[34] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[35..42] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[35..42] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[43..50] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[43..50] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[51..58] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[51..58] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[59..66] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[59..66] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[67..74] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[67..74] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_2[75..82] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[75..82] cast - sign_extend
OP E2274[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2275[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E2277[0] = FMOD7/uart_cpu.command[0..5] == 10	(unsigned, unsigned)
E2278[0] = E2277[0] AND E2274[0] AND E2275[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[0..8] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[0..8] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[9..20] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[9..20] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[21] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[21] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[22..33] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[22..33] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[34] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[34] cast - pad
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[35..42] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[35..42] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[43..50] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[43..50] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[51..58] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[51..58] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[59..66] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[59..66] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[67..74] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[67..74] cast - sign_extend
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[75..82] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_1[75..82] cast - sign_extend
E2289[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data_in_3[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82] cast - pad
DEL F2288 <- TS2271 CLK glob.c16 delay 1
OP E2300[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2312
	<-
	CLK       glob.c16
	START_IN  F2288
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F2307 <- SD2312 CLK glob.c16 delay 1
OP E2315[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.i[0..3] != 11	(unsigned, unsigned)
OP E2319[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data[0..87]	(unsigned, unsigned, unsigned)
S2322[0..7] = E2319[0..87] cast - pad
DEL F2318 <- SD2342 CLK glob.c16 delay 1
OP E2328[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.i[0..3] + 1	(unsigned, unsigned)
S2331[0..3] = E2328[0..4] cast - pad
OP E2335[0..87] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data[0..87] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2342
	<-
	CLK       glob.c16
	START_IN  SB2314
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB2314
	FINISH     F2345
	<-
	START      F2307
	TEST       E2315[0]
	CONTIN     F2318
	C          glob.c16
	RESET      null
}
DEL S2351 <- F2345 CLK glob.c16 delay 1
DEL F2355 <- S2351 CLK glob.c16 delay 1
DEL FF2357 <- FS2272 CLK glob.c16 delay 1
WHEN {
	T_START  TS2271
	F_START  FS2272
	FINISH   F2270
	<-
	START    S2269
	TEST     E2278[0]
	T_FINISH F2355
	F_FINISH FF2357
}
ILOOP  S2269 <- SSD1823 F2270
OP E2373[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2374[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E2376[0] = FMOD7/uart_cpu.command[0..5] == 11	(unsigned, unsigned)
E2377[0] = E2376[0] AND E2373[0] AND E2374[0]
E2388[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.in_r[0..11] cast - pad
DEL F2387 <- TS2370 CLK glob.c16 delay 1
OP E2399[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2411
	<-
	CLK       glob.c16
	START_IN  F2387
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F2406 <- SD2411 CLK glob.c16 delay 1
OP E2414[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.i[0..1] != 2	(unsigned, unsigned)
OP E2418[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.data[0..15]	(unsigned, unsigned, unsigned)
S2421[0..7] = E2418[0..15] cast - pad
DEL F2417 <- SD2441 CLK glob.c16 delay 1
OP E2427[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.i[0..1] + 1	(unsigned, unsigned)
S2430[0..1] = E2427[0..2] cast - pad
OP E2434[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.data[0..15] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2441
	<-
	CLK       glob.c16
	START_IN  SB2413
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB2413
	FINISH     F2444
	<-
	START      F2406
	TEST       E2414[0]
	CONTIN     F2417
	C          glob.c16
	RESET      null
}
DEL S2450 <- F2444 CLK glob.c16 delay 1
DEL F2454 <- S2450 CLK glob.c16 delay 1
DEL FF2456 <- FS2371 CLK glob.c16 delay 1
WHEN {
	T_START  TS2370
	F_START  FS2371
	FINISH   F2369
	<-
	START    S2368
	TEST     E2377[0]
	T_FINISH F2454
	F_FINISH FF2456
}
ILOOP  S2368 <- SSD1823 F2369
OP E2472[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2473[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E2475[0] = FMOD7/uart_cpu.command[0..5] == 12	(unsigned, unsigned)
E2476[0] = E2475[0] AND E2472[0] AND E2473[0]
E2487[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.in_r[0..19] cast - sign_extend
DEL F2486 <- TS2469 CLK glob.c16 delay 1
OP E2498[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2510
	<-
	CLK       glob.c16
	START_IN  F2486
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F2505 <- SD2510 CLK glob.c16 delay 1
OP E2513[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.i[0..1] != 3	(unsigned, unsigned)
OP E2517[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.data[0..23]	(unsigned, unsigned, unsigned)
S2520[0..7] = E2517[0..23] cast - pad
DEL F2516 <- SD2540 CLK glob.c16 delay 1
OP E2526[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.i[0..1] + 1	(unsigned, unsigned)
S2529[0..1] = E2526[0..2] cast - pad
OP E2533[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.data[0..23] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2540
	<-
	CLK       glob.c16
	START_IN  SB2512
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB2512
	FINISH     F2543
	<-
	START      F2505
	TEST       E2513[0]
	CONTIN     F2516
	C          glob.c16
	RESET      null
}
DEL S2549 <- F2543 CLK glob.c16 delay 1
DEL F2553 <- S2549 CLK glob.c16 delay 1
DEL FF2555 <- FS2470 CLK glob.c16 delay 1
WHEN {
	T_START  TS2469
	F_START  FS2470
	FINISH   F2468
	<-
	START    S2467
	TEST     E2476[0]
	T_FINISH F2553
	F_FINISH FF2555
}
ILOOP  S2467 <- SSD1823 F2468
OP E2571[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2572[0] = FMOD7/uart_cpu.rval[0] OR FMOD7/uart_cpu.rmem[0]
OP E2574[0] = FMOD7/uart_cpu.command[0..5] == 13	(unsigned, unsigned)
E2575[0] = E2574[0] AND E2571[0] AND E2572[0]
E2586[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.in_r[0..20] cast - pad
DEL F2585 <- TS2568 CLK glob.c16 delay 1
OP E2597[0] = ~VCC	(unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2609
	<-
	CLK       glob.c16
	START_IN  F2585
	BQAV      FMOD7/uart_cpu.tx.NF
}
DEL F2604 <- SD2609 CLK glob.c16 delay 1
OP E2612[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.i[0..1] != 3	(unsigned, unsigned)
OP E2616[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.empty[0]  ?  255 :  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.data[0..23]	(unsigned, unsigned, unsigned)
S2619[0..7] = E2616[0..23] cast - pad
DEL F2615 <- SD2639 CLK glob.c16 delay 1
OP E2625[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.i[0..1] + 1	(unsigned, unsigned)
S2628[0..1] = E2625[0..2] cast - pad
OP E2632[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.data[0..23] >> 8	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2639
	<-
	CLK       glob.c16
	START_IN  SB2611
	BQAV      FMOD7/uart_cpu.tx.NF
}
WHILE {
	START_B    SB2611
	FINISH     F2642
	<-
	START      F2604
	TEST       E2612[0]
	CONTIN     F2615
	C          glob.c16
	RESET      null
}
DEL S2648 <- F2642 CLK glob.c16 delay 1
DEL F2652 <- S2648 CLK glob.c16 delay 1
DEL FF2654 <- FS2569 CLK glob.c16 delay 1
WHEN {
	T_START  TS2568
	F_START  FS2569
	FINISH   F2567
	<-
	START    S2566
	TEST     E2575[0]
	T_FINISH F2652
	F_FINISH FF2654
}
ILOOP  S2566 <- SSD1823 F2567
OP E2665[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2666[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E2668[0] = FMOD7/uart_cpu.command[0..5] == 10	(unsigned, unsigned)
DEL F2673 <- TS2662 CLK glob.c16 delay 1
OP E2676[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.i[0..1] != 2	(unsigned, unsigned)
OP E2681[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.data[0..15] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2689
	<-
	CLK       glob.c16
	START_IN  SB2675
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F2680 <- SD2689 CLK glob.c16 delay 1
OP E2693[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.i[0..1] + 1	(unsigned, unsigned)
S2696[0..1] = E2693[0..2] cast - pad
OP E2702[0..15] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.data[0..15] >r> 8	(unsigned, unsigned)
DEL F2701 <- F2680 CLK glob.c16 delay 1
WHILE {
	START_B    SB2675
	FINISH     F2708
	<-
	START      F2673
	TEST       E2676[0]
	CONTIN     F2701
	C          glob.c16
	RESET      null
}
E2715[0..11] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.data[0..15] cast - pad
DEL F2714 <- F2708 CLK glob.c16 delay 1
DEL S2720 <- F2714 CLK glob.c16 delay 1
DEL F2724 <- S2720 CLK glob.c16 delay 1
AND2729[0] = FMOD7/uart_cpu.rx.NE AND E2668[0] AND E2665[0] AND E2666[0]
WHEN {
	T_START  TS2662
	F_START  FS2663
	FINISH   F2661
	<-
	START    S2660
	TEST     AND2729[0]
	T_FINISH F2724
	F_FINISH FF2664
}
DEL FF2664 <- FS2663 CLK glob.c16 delay 1
ILOOP  S2660 <- SSD1823 F2661
OP E2739[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2740[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E2742[0] = FMOD7/uart_cpu.command[0..5] == 11	(unsigned, unsigned)
DEL F2747 <- TS2736 CLK glob.c16 delay 1
OP E2750[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.i[0..1] != 3	(unsigned, unsigned)
OP E2755[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.data[0..23] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2763
	<-
	CLK       glob.c16
	START_IN  SB2749
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F2754 <- SD2763 CLK glob.c16 delay 1
OP E2767[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.i[0..1] + 1	(unsigned, unsigned)
S2770[0..1] = E2767[0..2] cast - pad
OP E2776[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.data[0..23] >r> 8	(unsigned, unsigned)
DEL F2775 <- F2754 CLK glob.c16 delay 1
WHILE {
	START_B    SB2749
	FINISH     F2782
	<-
	START      F2747
	TEST       E2750[0]
	CONTIN     F2775
	C          glob.c16
	RESET      null
}
E2789[0..19] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.data[0..23] cast - sign_extend
DEL F2788 <- F2782 CLK glob.c16 delay 1
DEL S2794 <- F2788 CLK glob.c16 delay 1
DEL F2798 <- S2794 CLK glob.c16 delay 1
AND2803[0] = FMOD7/uart_cpu.rx.NE AND E2742[0] AND E2739[0] AND E2740[0]
WHEN {
	T_START  TS2736
	F_START  FS2737
	FINISH   F2735
	<-
	START    S2734
	TEST     AND2803[0]
	T_FINISH F2798
	F_FINISH FF2738
}
DEL FF2738 <- FS2737 CLK glob.c16 delay 1
ILOOP  S2734 <- SSD1823 F2735
OP E2813[0] = ~FMOD7/uart_cpu.idle[0]	(unsigned)
E2814[0] = FMOD7/uart_cpu.wstat[0] OR FMOD7/uart_cpu.wmem[0]
OP E2816[0] = FMOD7/uart_cpu.command[0..5] == 12	(unsigned, unsigned)
DEL F2821 <- TS2810 CLK glob.c16 delay 1
OP E2824[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.i[0..1] != 3	(unsigned, unsigned)
OP E2829[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.data[0..23] | FMOD7/uart_cpu.rx[0..7]	(unsigned, unsigned)
EXECP no priority, buffered queues only {
	START_DEL SD2837
	<-
	CLK       glob.c16
	START_IN  SB2823
	BQAV      FMOD7/uart_cpu.rx.NE
}
DEL F2828 <- SD2837 CLK glob.c16 delay 1
OP E2841[0..2] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.i[0..1] + 1	(unsigned, unsigned)
S2844[0..1] = E2841[0..2] cast - pad
OP E2850[0..23] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.data[0..23] >r> 8	(unsigned, unsigned)
DEL F2849 <- F2828 CLK glob.c16 delay 1
WHILE {
	START_B    SB2823
	FINISH     F2856
	<-
	START      F2821
	TEST       E2824[0]
	CONTIN     F2849
	C          glob.c16
	RESET      null
}
E2863[0..20] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.data[0..23] cast - pad
DEL F2862 <- F2856 CLK glob.c16 delay 1
DEL S2868 <- F2862 CLK glob.c16 delay 1
DEL F2872 <- S2868 CLK glob.c16 delay 1
AND2877[0] = FMOD7/uart_cpu.rx.NE AND E2816[0] AND E2813[0] AND E2814[0]
WHEN {
	T_START  TS2810
	F_START  FS2811
	FINISH   F2809
	<-
	START    S2808
	TEST     AND2877[0]
	T_FINISH F2872
	F_FINISH FF2812
}
DEL FF2812 <- FS2811 CLK glob.c16 delay 1
ILOOP  S2808 <- SSD1823 F2809
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.in0_1[0..4] = prog.receive_event_0.sigint_0.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.in1_1[0..4] = prog.receive_event_1.sigint_1.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.select1[0]
OP E2887[0] = ~prog.receive_event_1.sigint_1.q.NE	(unsigned)
OP E2888[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.select1_1[0]	(unsigned)
E2889[0] = E2887[0] OR E2888[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.par_56.assign_port_0.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.in0_1[0..4]
S2896[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.par_56.assign_port_0.in_1[0..4] cast - pad
AND2909[0] = E2889[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.NF AND prog.receive_event_0.sigint_0.q.NE
WHEN {
	T_START  TS2881
	F_START  FS2882
	FINISH   -
	<-
	START    SDD3406
	TEST     AND2909[0]
	T_FINISH -
	F_FINISH -
}
OP E2918[0] = ~prog.receive_event_0.sigint_0.q.NE	(unsigned)
E2919[0] = E2918[0] OR FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.select1_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.par_57.assign_port_1.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.in1_1[0..4]
S2926[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.mux2_0.par_57.assign_port_1.in_1[0..4] cast - pad
OR2935[0] = TS2881 OR TS2913
AND2939[0] = E2919[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.NF AND prog.receive_event_1.sigint_1.q.NE
WHEN {
	T_START  TS2913
	F_START  FS2914
	FINISH   -
	<-
	START    SDD3407
	TEST     AND2939[0]
	T_FINISH -
	F_FINISH -
}
OP E2949[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.select1[0]	(unsigned)
WHEN {
	T_START  TS2943
	F_START  FS2944
	FINISH   -
	<-
	START    SDD3401
	TEST     OR2935[0]
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.in0_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1[0..6]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.in1_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2[0..6]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.select1_1[0]
OP E2963[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.NE	(unsigned)
OP E2964[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.select1_1[0]	(unsigned)
E2965[0] = E2963[0] OR E2964[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.par_58.assign_port_2.in_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.in0_1[0..6]
AND2984[0] = E2965[0] AND FMOD7/uart_cpu.iq.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.NE
WHEN {
	T_START  TS2957
	F_START  FS2958
	FINISH   -
	<-
	START    SDD3408
	TEST     AND2984[0]
	T_FINISH -
	F_FINISH -
}
OP E2993[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.NE	(unsigned)
E2994[0] = E2993[0] OR FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.select1_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.par_59.assign_port_3.in_1[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.in1_1[0..6]
OR3009[0] = TS2957 OR TS2988
AND3013[0] = E2994[0] AND FMOD7/uart_cpu.iq.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.NE
WHEN {
	T_START  TS2988
	F_START  FS2989
	FINISH   -
	<-
	START    SDD3409
	TEST     AND3013[0]
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.in0_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.sigint_2.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.in1_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.sigint_3.q[0..4]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.select1[0]
OP E3023[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.sigint_3.q.NE	(unsigned)
OP E3024[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.select1_1[0]	(unsigned)
E3025[0] = E3023[0] OR E3024[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.par_60.assign_port_4.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.in0_1[0..4]
S3032[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.par_60.assign_port_4.in_1[0..4] cast - pad
AND3045[0] = E3025[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.sigint_2.q.NE
WHEN {
	T_START  TS3017
	F_START  FS3018
	FINISH   -
	<-
	START    SDD3410
	TEST     AND3045[0]
	T_FINISH -
	F_FINISH -
}
OP E3054[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.sigint_2.q.NE	(unsigned)
E3055[0] = E3054[0] OR FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.select1_1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.par_61.assign_port_5.in_1[0..4] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.in1_1[0..4]
S3062[0..6] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.mux2_2.par_61.assign_port_5.in_1[0..4] cast - pad
OR3071[0] = TS3017 OR TS3049
AND3075[0] = E3055[0] AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.NF AND FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.sigint_3.q.NE
WHEN {
	T_START  TS3049
	F_START  FS3050
	FINISH   -
	<-
	START    SDD3411
	TEST     AND3075[0]
	T_FINISH -
	F_FINISH -
}
OP E3085[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.select1[0]	(unsigned)
WHEN {
	T_START  TS3079
	F_START  FS3080
	FINISH   -
	<-
	START    SDD3402
	TEST     OR3071[0]
	T_FINISH -
	F_FINISH -
}
OP E3099[0] = ~FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_555.s1[0]	(unsigned)
WHEN {
	T_START  TS3093
	F_START  FS3094
	FINISH   -
	<-
	START    SDD3403
	TEST     OR3009[0]
	T_FINISH -
	F_FINISH -
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.select1_1[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_555.s1[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.select1_2[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_555.s1[0]
OP E3117[0..7] = 128 | FMOD7/uart_cpu.iq[0..6]	(unsigned, unsigned)
AV3127 = FMOD7/uart_cpu.tx.NF AND FMOD7/uart_cpu.iq.NE
AND3128[0] = FMOD7/uart_cpu.idle[0] AND AV3127
WHEN {
	T_START  TS3110
	F_START  FS3111
	FINISH   -
	<-
	START    SDD3412
	TEST     AND3128[0]
	T_FINISH -
	F_FINISH -
}
NCF "NET PORT_C10 IOSTANDARD=LVCMOS33;" port PORT_C10
NCF "NET %n TNM=C16;" port null
NCF "TIMESPEC TS_C16=PERIOD C16 62.5;" port null
NCF "NET PORT_A3 IOSTANDARD=LVCMOS33;" port PORT_A3
NCF "NET PORT_B3 IOSTANDARD=LVCMOS33;" port PORT_B3
REG
	OUT  prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforeset[0]
	<-
	CLK  glob.c16
	D    TS690
	CE   SDD3395
	R    GND
    {0x0}
REG
	OUT  prog.FMOD1/aes_sp3a_eval400.FMOD7/uart_cpu.inforead[0]
	<-
	CLK  glob.c16
	D    F664
	CE   SDD3394
	R    GND
    {0x0}
prog.r.D[0..15] = E774[0..15]
REG
	OUT  prog.r[0..15]
	<-
	CLK  glob.c16
	D    prog.r.D[0..15]
	CE   F767
	R    GND
    {0x0137}
prog.rr.D[0..34] = E947[0..34]
REG
	OUT  prog.rr[0..34]
	<-
	CLK  glob.c16
	D    prog.rr.D[0..34]
	CE   F940
	R    GND
    {0x0}
prog.rrr.D[0..5] = E1120[0..5]
REG
	OUT  prog.rrr[0..5]
	<-
	CLK  glob.c16
	D    prog.rrr.D[0..5]
	CE   F1113
	R    GND
    {0x0}
REG
	OUT  prog.rrra[0]
	<-
	CLK  glob.c16
	D    F1119
	CE   SDD3396
	R    GND
    {0x0}
prog.receive_event_0.sigint_0.q.D[0..4] = GND cast - sign_extend
QUEUEBUFFER  depth 2 {
	OUT      prog.receive_event_0.sigint_0.q[0..4]
	NE       prog.receive_event_0.sigint_0.q.NE
	NF       prog.receive_event_0.sigint_0.q.NF
	<-
	CLK      glob.c16
	DATA     prog.receive_event_0.sigint_0.q.D[0..4]
	PUSH     TS5
	POP      TS2881
	RESET    GND
}
prog.q.D[0..13] = E1301[0..13]
QUEUEBUFFER  depth 16 {
	OUT      prog.q[0..13]
	NE       prog.q.NE
	NF       prog.q.NF
	CNT      prog.q.CNT[0..4]
	SPC      prog.q.SPC[0..4]
	<-
	CLK      glob.c16
	DATA     prog.q.D[0..13]
	PUSH     TS1294
	POP      prog.q.POP
	RESET    GND
}
prog.q.POP = SD1539 OR SD1555
QUEUEBUFFER  depth 16 {
	OUT      prog.qo[0..13]
	NE       prog.qo.NE
	NF       prog.qo.NF
	<-
	CLK      glob.c16
	DATA     prog.q[0..13]
	PUSH     SD1555
	POP      SD1860
	RESET    GND
}
REG
	OUT  prog.acc[0]
	<-
	CLK  glob.c16
	D    F1613
	CE   SDD3397
	R    GND
    {0x0}
REG
	OUT  prog.count[0..4]
	<-
	CLK  glob.c16
	D    prog.q.CNT[0..4]
	CE   SDD3398
	R    GND
    {0x0}
QUEUEBUFFER  depth 2 {
	OUT      prog.receive_event_1.sigint_1.q[0..4]
	NE       prog.receive_event_1.sigint_1.q.NE
	NF       prog.receive_event_1.sigint_1.q.NF
	<-
	CLK      glob.c16
	DATA     3
	PUSH     TS34
	POP      TS2913
	RESET    GND
}
SELECT {
	OUT  S3142[0..7]
	<-
	SEL  TS2130
	IN   MADDR2136[0..7]
	SEL  TS2007
	IN   MADDR2014[0..7]
}
S3145[0..15] = MDATA2016[0..15]
RRAM - 1 ports {
	OUT0	prog.m_0[0..15]
	<-
	CLK0	glob.c16
	ADDR0	S3142[0..7]
	DATA0	S3145[0..15]
	RE0	TS2130
	WE0	TS2007
    initialised
}
prog.s.RES[0..8] = GND expand
prog.s.RES[9..20] = GND expand
prog.s.RES[21] = GND
prog.s.RES[22..33] = GND expand
prog.s.RES[34] = GND
prog.s.RES[35..42] = GND expand
prog.s.RES[43..50] = GND expand
prog.s.RES[51..58] = GND expand
prog.s.RES[59..66] = GND expand
prog.s.RES[67..74] = GND expand
prog.s.RES[75..82] = GND expand
prog.s.D[0..8] = S2215[0..8]
prog.s.D[9..20] = S2216[0..11]
prog.s.D[21] = S2217[0]
prog.s.D[22..33] = S2218[0..11]
prog.s.D[34] = S2219[0]
prog.s.D[35..42] = S2220[0..7]
prog.s.D[43..50] = S2221[0..7]
prog.s.D[51..58] = S2222[0..7]
prog.s.D[59..66] = S2223[0..7]
prog.s.D[67..74] = S2224[0..7]
prog.s.D[75..82] = S2225[0..7]
prog.s.CE[0..8] = F2205 expand
prog.s.CE[9..20] = F2205 expand
prog.s.CE[21] = F2205
prog.s.CE[22..33] = F2205 expand
prog.s.CE[34] = F2205
prog.s.CE[35..42] = F2205 expand
prog.s.CE[43..50] = F2205 expand
prog.s.CE[51..58] = F2205 expand
prog.s.CE[59..66] = F2205 expand
prog.s.CE[67..74] = F2205 expand
prog.s.CE[75..82] = F2205 expand
REG
	OUT  prog.s[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
	<-
	CLK  glob.c16
	D    prog.s.D[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
	CE   prog.s.CE[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
	R    prog.s.RES[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
    {0x7f008003822041ec001ff}
prog.uf.D[0..11] = E2715[0..11]
REG
	OUT  prog.uf[0..11]
	<-
	CLK  glob.c16
	D    prog.uf.D[0..11]
	CE   F2708
	R    GND
    {0x204}
prog.f.D[0..19] = E2789[0..19]
REG
	OUT  prog.f[0..19]
	<-
	CLK  glob.c16
	D    prog.f.D[0..19]
	CE   F2782
	R    GND
    {0xfd780}
prog.fl.D[0..20] = E2863[0..20]
REG
	OUT  prog.fl[0..20]
	<-
	CLK  glob.c16
	D    prog.fl.D[0..20]
	CE   F2856
	R    GND
    {0x17f800}
OR3150 = S1619 OR S1401 OR S2120 OR S2450 OR S1922 OR S2549 OR S2351 OR S2648 OR S880 OR S670 OR S1231 OR S1715 OR S1053
REG
	OUT  FMOD7/uart_cpu.rval[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS435
	R    OR3150
    {0x0}
OR3152 = S2868 OR S1469 OR S1125 OR S779 OR S701 OR S1784 OR S2228 OR S2794 OR S1309 OR S1994 OR S2720 OR S952
REG
	OUT  FMOD7/uart_cpu.wstat[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS443
	R    OR3152
    {0x0}
OR3154 = S1619 OR S1401 OR S2120 OR S2450 OR S1922 OR S2549 OR S2351 OR S2648 OR S880 OR S670 OR S1231 OR S1715 OR S1053
REG
	OUT  FMOD7/uart_cpu.rmem[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS451
	R    OR3154
    {0x0}
OR3156 = S2868 OR S1469 OR S1125 OR S779 OR S701 OR S1784 OR S2228 OR S2794 OR S1309 OR S1994 OR S2720 OR S952
REG
	OUT  FMOD7/uart_cpu.wmem[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   CS459
	R    OR3156
    {0x0}
OR3160 = TS2957 OR TS2988
SELECT {
	OUT  FMOD7/uart_cpu.iq.D[0..6]
	<-
	SEL  TS2957
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.par_58.assign_port_2.in_1[0..6]
	SEL  TS2988
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.mux2_1.par_59.assign_port_3.in_1[0..6]
    unselected out 0x0
}
QUEUEBUFFER  depth 16 {
	OUT      FMOD7/uart_cpu.iq[0..6]
	NE       FMOD7/uart_cpu.iq.NE
	NF       FMOD7/uart_cpu.iq.NF
	<-
	CLK      glob.c16
	DATA     FMOD7/uart_cpu.iq.D[0..6]
	PUSH     OR3160
	POP      TS3110
	RESET    GND
}
IBUF  INPUT3161[0] <- PORT_A3 loc=A3 id b620 IBUF
FMOD7/uart_cpu.rx.D[0..7] = E268[0..7]
QUEUEBUFFER  depth 16 {
	OUT      FMOD7/uart_cpu.rx[0..7]
	NE       FMOD7/uart_cpu.rx.NE
	NF       FMOD7/uart_cpu.rx.NF
	<-
	CLK      glob.c16
	DATA     FMOD7/uart_cpu.rx.D[0..7]
	PUSH     TS224
	POP      FMOD7/uart_cpu.rx.POP
	RESET    GND
}
FMOD7/uart_cpu.rx.POP = SD431 OR SD489 OR SD502 OR SD748 OR SD921 OR SD1094 OR SD1269 OR SD1438 OR SD1753 OR SD1963 OR SD2186 OR SD2689 OR SD2763 OR SD2837
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l.D[0..10] = S90[0..10]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l[0..10]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.l.D[0..10]
	CE   SDD3393
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.prev[0]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc16_0.to_pulse_0.in_1[0]
	CE   SDD3392
	R    GND
    {0x1}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data.D[0..7] = S189[0..7]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.data.D[0..7]
	CE   F176
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount.D[0..3] = S196[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.bitcount.D[0..3]
	CE   F176
	R    TS132
    {0x8}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.ferr[0]
	<-
	CLK  glob.c16
	D    E219[0]
	CE   F212
	R    TS132
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.oflo[0]
	<-
	CLK  glob.c16
	D    VCC
	CE   FS225
	R    TS132
    {0x0}
OR3173 = TS132 OR F160 OR F147
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count.D[0..3] = S109[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.brc_count.D[0..3]
	CE   TS99
	R    OR3173
    {0x9}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]
	<-
	CLK  glob.c16
	D    INPUT3161[0]
	CE   TS99
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16_prev[0]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.uart_rx_0.rxd16[0]
	CE   TS99
	R    GND
    {0x0}
OR3204 = SD1883 OR SD1222 OR SD2342 OR SD2411 OR SD2609 OR SD2540 OR SD1610 OR SD1706 OR TS3110 OR SD1193 OR SD2111 OR SD1363 OR SD1392 OR SD1913 OR SD2441 OR SD2639 OR SD2510 OR SD1044 OR SD1677 OR SD2081 OR SD631 OR SD1580 OR SD1014 OR SD871 OR SD841 OR SD2312 OR SD661
SELECT {
	OUT  FMOD7/uart_cpu.tx.D[0..7]
	<-
	SEL  SD631
	IN   4
	SEL  SD661
	IN   S641[0..7]
	SEL  SD841
	IN   2
	SEL  SD871
	IN   S851[0..7]
	SEL  SD1014
	IN   5
	SEL  SD1044
	IN   S1024[0..7]
	SEL  SD1193
	IN   1
	SEL  SD1222
	IN   E1200[0..7]
	SEL  SD1363
	IN   1
	SEL  SD1392
	IN   E1370[0..7]
	SEL  SD1580
	IN   2
	SEL  SD1610
	IN   S1590[0..7]
	SEL  SD1677
	IN   1
	SEL  SD1706
	IN   E1684[0..7]
	SEL  SD1883
	IN   2
	SEL  SD1913
	IN   S1893[0..7]
	SEL  SD2081
	IN   2
	SEL  SD2111
	IN   S2091[0..7]
	SEL  SD2312
	IN   11
	SEL  SD2342
	IN   S2322[0..7]
	SEL  SD2411
	IN   2
	SEL  SD2441
	IN   S2421[0..7]
	SEL  SD2510
	IN   3
	SEL  SD2540
	IN   S2520[0..7]
	SEL  SD2609
	IN   3
	SEL  SD2639
	IN   S2619[0..7]
	SEL  TS3110
	IN   E3117[0..7]
    unselected out 0x0
}
QUEUEBUFFER  depth 16 {
	OUT      FMOD7/uart_cpu.tx[0..7]
	NE       FMOD7/uart_cpu.tx.NE
	NF       FMOD7/uart_cpu.tx.NF
	<-
	CLK      glob.c16
	DATA     FMOD7/uart_cpu.tx.D[0..7]
	PUSH     OR3204
	POP      SD340
	RESET    GND
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.OUTPUT0[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.tx_out_1[0]
OBUF PORT_B3 <- OUTPUTBIT3205[0] loc=B3 id b656 OBUF
OUTPUTBIT3205[0] = FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.OUTPUT0[0] cast - pad
OR3206 = F400 OR F386
OR3209 = SD340 OR F350
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.txd_.D[0]
	<-
	SEL  F350
	IN   E357[0]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.txd_[0]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.txd_.D[0]
	CE   OR3209
	R    OR3206
    {0x1}
OR3210 = FS297 OR SD340
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.txbusy_[0]
	<-
	CLK  glob.c16
	D    GND
	CE   GND
	R    OR3210
    {0x1}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.bitcount.D[0..3] = S372[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.bitcount[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.bitcount.D[0..3]
	CE   F362
	R    SD340
    {0x8}
OR3215 = F356 OR SD340
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.data.D[0..7]
	<-
	SEL  F356
	IN   E363[0..7]
	SEL  SD340
	IN   E282[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.cpu_interface_0.if_367.uart_tx_0.data.D[0..7]
	CE   OR3215
	R    GND
    {0x0}
FMOD7/uart_cpu.command.RES[0..5] = GND expand
FMOD7/uart_cpu.command.RES[6..7] = GND expand
FMOD7/uart_cpu.command.D[0..5] = S424[0..5]
FMOD7/uart_cpu.command.D[6..7] = S425[0..1]
FMOD7/uart_cpu.command.CE[0..5] = SD431 expand
FMOD7/uart_cpu.command.CE[6..7] = SD431 expand
REG
	OUT  FMOD7/uart_cpu.command[0..5,6..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.command.D[0..5,6..7]
	CE   FMOD7/uart_cpu.command.CE[0..5,6..7]
	R    FMOD7/uart_cpu.command.RES[0..5,6..7]
    {0x0}
OR3219 = SD489 OR SD502
SELECT {
	OUT  FMOD7/uart_cpu.memaddr.D[0..15]
	<-
	SEL  SD502
	IN   E495[0..15]
	SEL  SD489
	IN   S484[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.memaddr[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.memaddr.D[0..15]
	CE   OR3219
	R    GND
    {0x0}
OR3220 = S1469 OR S1619 OR S701 OR S1309 OR S1994 OR S2549 OR S2351 OR S2868 OR S880 OR S2228 OR S2794 OR S2720 OR S952 OR S1053 OR S779 OR S1401 OR S2120 OR S2450 OR S1922 OR S2648 OR S1125 OR S670 OR S1231 OR S1715 OR S1784
REG
	OUT  FMOD7/uart_cpu.idle[0]
	<-
	CLK  glob.c16
	D    GND
	CE   F473
	R    OR3220
    {0x1}
RRAM - 1 ports {
	OUT0	FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.im_0[0..31]
	<-
	CLK0	glob.c16
	ADDR0	MADDR558[0..8]
	DATA0	-
	RE0	TS551
	WE0	-
    initialised
}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address.D[0..8] = S572[0..8]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address[0..8]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.address.D[0..8]
	CE   TS562
	R    TS532
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.in_r[0..31]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.compressInfo_0.ILM10_0.im_0[0..31]
	CE   VCC
	R    GND
    {0x0}
OR3228 = TS590 OR SD661
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.data.D[0..31]
	<-
	SEL  SD661
	IN   E654[0..31]
	SEL  TS590
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.in_r[0..31]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.data[0..31]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.data.D[0..31]
	CE   OR3228
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.empty[0]
	<-
	CLK  glob.c16
	D    E619[0]
	CE   TS590
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.i.D[0..2] = S650[0..2]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.i[0..2]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_457.read_value_gen_0.read_10.i.D[0..2]
	CE   SD661
	R    TS590
    {0x0}
OR3235 = F739 OR SD748
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.data.D[0..15]
	<-
	SEL  SD748
	IN   E740[0..15]
	SEL  F739
	IN   E761[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.data.D[0..15]
	CE   OR3235
	R    TS721
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.i.D[0..1] = S755[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_459.write_static_gen_1.if_485.write_7.i.D[0..1]
	CE   SB734
	R    TS721
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.in_r.D[0..15] = prog.read_1.case_25.read_value_1.in_1[0..15]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.in_r[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.in_r.D[0..15]
	CE   VCC
	R    GND
    {0x0}
OR3241 = SD871 OR TS800
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.data.D[0..15]
	<-
	SEL  SD871
	IN   E864[0..15]
	SEL  TS800
	IN   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.in_r[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.data.D[0..15]
	CE   OR3241
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.empty[0]
	<-
	CLK  glob.c16
	D    E829[0]
	CE   TS800
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.i.D[0..1] = S860[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_460.read_value_gen_1.read_11.i.D[0..1]
	CE   SD871
	R    TS800
    {0x0}
OR3248 = F912 OR SD921
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.data.D[0..39]
	<-
	SEL  F912
	IN   E934[0..39]
	SEL  SD921
	IN   E913[0..39]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.data[0..39]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.data.D[0..39]
	CE   OR3248
	R    TS894
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.i.D[0..2] = S928[0..2]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.i[0..2]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_461.write_static_gen_2.if_491.write_8.i.D[0..2]
	CE   SB907
	R    TS894
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.in_r.D[0..34] = prog.read_2.case_28.read_value_2.in_1[0..34]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.in_r[0..34]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.in_r.D[0..34]
	CE   VCC
	R    GND
    {0x0}
OR3254 = TS973 OR SD1044
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.data.D[0..39]
	<-
	SEL  TS973
	IN   E991[0..39]
	SEL  SD1044
	IN   E1037[0..39]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.data[0..39]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.data.D[0..39]
	CE   OR3254
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.empty[0]
	<-
	CLK  glob.c16
	D    E1002[0]
	CE   TS973
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.i.D[0..2] = S1033[0..2]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.i[0..2]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_462.read_value_gen_2.read_12.i.D[0..2]
	CE   SD1044
	R    TS973
    {0x0}
OR3261 = F1085 OR SD1094
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.data.D[0..7]
	<-
	SEL  F1085
	IN   E1107[0..7]
	SEL  SD1094
	IN   E1086[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.data.D[0..7]
	CE   OR3261
	R    TS1067
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_463.write_static_gen_3.if_499.write_9.i[0]
	<-
	CLK  glob.c16
	D    S1101[0]
	CE   SB1080
	R    TS1067
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.in_r.D[0..5] = prog.read_3.case_31.read_value_3.in_1[0..5]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.in_r[0..5]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.in_r.D[0..5]
	CE   VCC
	R    GND
    {0x0}
OR3267 = SD1222 OR TS1152
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.data.D[0..7]
	<-
	SEL  TS1152
	IN   E1170[0..7]
	SEL  SD1222
	IN   E1215[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.data.D[0..7]
	CE   OR3267
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.empty[0]
	<-
	CLK  glob.c16
	D    E1181[0]
	CE   TS1152
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_464.read_value_gen_3.read_13.i[0]
	<-
	CLK  glob.c16
	D    S1211[0]
	CE   SD1222
	R    TS1152
    {0x0}
OR3274 = F1260 OR SD1269
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.data.D[0..15]
	<-
	SEL  F1260
	IN   E1282[0..15]
	SEL  SD1269
	IN   E1261[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.data.D[0..15]
	CE   OR3274
	R    TS1242
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.i.D[0..1] = S1276[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.write_10.i.D[0..1]
	CE   SB1255
	R    TS1242
    {0x0}
OR3279 = SD1392 OR TS1322
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.data.D[0..7]
	<-
	SEL  TS1322
	IN   E1340[0..7]
	SEL  SD1392
	IN   E1385[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.data.D[0..7]
	CE   OR3279
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.empty[0]
	<-
	CLK  glob.c16
	D    E1351[0]
	CE   TS1322
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_506.read_14.i[0]
	<-
	CLK  glob.c16
	D    S1381[0]
	CE   SD1392
	R    TS1322
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.ilevel.D[0..3] = E1464[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.ilevel[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.ilevel.D[0..3]
	CE   F1457
	R    GND
    {0x1}
OR3287 = F1430 OR SD1438
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.data.D[0..7]
	<-
	SEL  F1430
	IN   E1451[0..7]
	SEL  SD1438
	IN   E1431[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.data.D[0..7]
	CE   OR3287
	R    TS1412
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.write_11.i[0]
	<-
	CLK  glob.c16
	D    S1445[0]
	CE   SB1425
	R    TS1412
    {0x0}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.sigint_2.q[0..4]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.sigint_2.q.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_465.write_queue_gen_0.if_507.if_508.if_509.sigint_2.q.NF
	<-
	CLK      glob.c16
	DATA     1
	PUSH     TS1482
	POP      TS3017
	RESET    GND
}
OR3293 = SD1610 OR SD1539
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.data.D[0..15]
	<-
	SEL  SD1610
	IN   E1603[0..15]
	SEL  SD1539
	IN   E1532[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.data.D[0..15]
	CE   OR3293
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.empty[0]
	<-
	CLK  glob.c16
	D    E1568[0]
	CE   TS1514
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.i.D[0..1] = S1599[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.read_15.i.D[0..1]
	CE   SD1610
	R    TS1514
    {0x0}
OR3299 = TS1636 OR SD1706
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.data.D[0..7]
	<-
	SEL  TS1636
	IN   E1654[0..7]
	SEL  SD1706
	IN   E1699[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.data.D[0..7]
	CE   OR3299
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.empty[0]
	<-
	CLK  glob.c16
	D    E1665[0]
	CE   TS1636
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_514.read_16.i[0]
	<-
	CLK  glob.c16
	D    S1695[0]
	CE   SD1706
	R    TS1636
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.ilevel.D[0..3] = E1779[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.ilevel[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.ilevel.D[0..3]
	CE   F1772
	R    GND
    {0x1}
OR3307 = SD1753 OR F1744
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.data.D[0..7]
	<-
	SEL  SD1753
	IN   E1745[0..7]
	SEL  F1744
	IN   E1766[0..7]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.data[0..7]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.data.D[0..7]
	CE   OR3307
	R    TS1726
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.write_12.i[0]
	<-
	CLK  glob.c16
	D    S1760[0]
	CE   SB1739
	R    TS1726
    {0x0}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.sigint_3.q[0..4]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.sigint_3.q.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_466.read_queue_gen_0.if_515.if_516.if_517.sigint_3.q.NF
	<-
	CLK      glob.c16
	DATA     2
	PUSH     TS1797
	POP      TS3049
	RESET    GND
}
OR3313 = SD1913 OR SD1860
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.data.D[0..15]
	<-
	SEL  SD1913
	IN   E1906[0..15]
	SEL  SD1860
	IN   E1853[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.data.D[0..15]
	CE   OR3313
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.empty[0]
	<-
	CLK  glob.c16
	D    E1871[0]
	CE   TS1835
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.i.D[0..1] = S1902[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_467.read_queue_gen_1.read_17.i.D[0..1]
	CE   SD1913
	R    TS1835
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.mdata.D[0..15] = E1989[0..15]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.mdata[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.mdata.D[0..15]
	CE   F1982
	R    GND
    {0x0}
OR3321 = SD1963 OR F1954
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.data.D[0..15]
	<-
	SEL  SD1963
	IN   E1955[0..15]
	SEL  F1954
	IN   E1976[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.data.D[0..15]
	CE   OR3321
	R    TS1936
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.i.D[0..1] = S1970[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_468.write_memory_gen_0.write_13.i.D[0..1]
	CE   SB1949
	R    TS1936
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.data[0..15]
	<-
	CLK  glob.c16
	D    prog.m_0[0..15]
	CE   S2135
	R    GND
    {0x0}
OR3327 = F2044 OR SD2111
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.data.D[0..15]
	<-
	SEL  F2044
	IN   E2058[0..15]
	SEL  SD2111
	IN   E2104[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.data.D[0..15]
	CE   OR3327
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.empty[0]
	<-
	CLK  glob.c16
	D    E2069[0]
	CE   F2044
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.i.D[0..1] = S2100[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_469.read_memory_gen_0.if_526.read_18.i.D[0..1]
	CE   SD2111
	R    F2044
    {0x0}
OR3334 = SD2186 OR F2177
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.data.D[0..87]
	<-
	SEL  SD2186
	IN   E2178[0..87]
	SEL  F2177
	IN   E2199[0..87]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.data[0..87]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.data.D[0..87]
	CE   OR3334
	R    TS2159
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.i.D[0..3] = S2193[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.i[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_470.write_static_gen_4.if_531.write_14.i.D[0..3]
	CE   SB2172
	R    TS2159
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[0..8] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[9..20] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[21] = GND
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[22..33] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[34] = GND
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[35..42] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[43..50] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[51..58] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[59..66] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[67..74] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[75..82] = GND expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[0..8] = S2247[0..8]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[9..20] = S2248[0..11]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[21] = S2249[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[22..33] = S2250[0..11]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[34] = S2251[0]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[35..42] = S2252[0..7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[43..50] = S2253[0..7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[51..58] = S2254[0..7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[59..66] = S2255[0..7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[67..74] = S2256[0..7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[75..82] = S2257[0..7]
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[0..8] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[9..20] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[21] = VCC
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[22..33] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[34] = VCC
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[35..42] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[43..50] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[51..58] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[59..66] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[67..74] = VCC expand
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[75..82] = VCC expand
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.D[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
	CE   FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.CE[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
	R    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.in_r.RES[0..8,9..20,21,22..33,34,35..42,43..50,51..58,59..66,67..74,75..82]
    {0x0}
OR3340 = SD2342 OR TS2271
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data.D[0..87]
	<-
	SEL  TS2271
	IN   E2289[0..87]
	SEL  SD2342
	IN   E2335[0..87]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data[0..87]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.data.D[0..87]
	CE   OR3340
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.empty[0]
	<-
	CLK  glob.c16
	D    E2300[0]
	CE   TS2271
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.i.D[0..3] = S2331[0..3]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.i[0..3]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_471.read_value_gen_4.read_19.i.D[0..3]
	CE   SD2342
	R    TS2271
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.in_r.D[0..11] = prog.read_7.case_45.read_value_5.in_1[0..11]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.in_r[0..11]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.in_r.D[0..11]
	CE   VCC
	R    GND
    {0x0}
OR3347 = SD2441 OR TS2370
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.data.D[0..15]
	<-
	SEL  TS2370
	IN   E2388[0..15]
	SEL  SD2441
	IN   E2434[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.data.D[0..15]
	CE   OR3347
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.empty[0]
	<-
	CLK  glob.c16
	D    E2399[0]
	CE   TS2370
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.i.D[0..1] = S2430[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_472.read_value_gen_5.read_20.i.D[0..1]
	CE   SD2441
	R    TS2370
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.in_r.D[0..19] = prog.read_8.case_47.read_value_6.in_1[0..19]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.in_r[0..19]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.in_r.D[0..19]
	CE   VCC
	R    GND
    {0x0}
OR3354 = SD2540 OR TS2469
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.data.D[0..23]
	<-
	SEL  SD2540
	IN   E2533[0..23]
	SEL  TS2469
	IN   E2487[0..23]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.data[0..23]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.data.D[0..23]
	CE   OR3354
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.empty[0]
	<-
	CLK  glob.c16
	D    E2498[0]
	CE   TS2469
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.i.D[0..1] = S2529[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_473.read_value_gen_6.read_21.i.D[0..1]
	CE   SD2540
	R    TS2469
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.in_r.D[0..20] = prog.read_9.case_49.read_value_7.in_1[0..20]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.in_r[0..20]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.in_r.D[0..20]
	CE   VCC
	R    GND
    {0x0}
OR3361 = TS2568 OR SD2639
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.data.D[0..23]
	<-
	SEL  TS2568
	IN   E2586[0..23]
	SEL  SD2639
	IN   E2632[0..23]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.data[0..23]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.data.D[0..23]
	CE   OR3361
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.empty[0]
	<-
	CLK  glob.c16
	D    E2597[0]
	CE   TS2568
	R    GND
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.i.D[0..1] = S2628[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_474.read_value_gen_7.read_22.i.D[0..1]
	CE   SD2639
	R    TS2568
    {0x0}
OR3368 = SD2689 OR F2680
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.data.D[0..15]
	<-
	SEL  SD2689
	IN   E2681[0..15]
	SEL  F2680
	IN   E2702[0..15]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.data[0..15]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.data.D[0..15]
	CE   OR3368
	R    TS2662
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.i.D[0..1] = S2696[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_475.write_static_gen_5.if_540.write_15.i.D[0..1]
	CE   SB2675
	R    TS2662
    {0x0}
OR3374 = F2754 OR SD2763
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.data.D[0..23]
	<-
	SEL  SD2763
	IN   E2755[0..23]
	SEL  F2754
	IN   E2776[0..23]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.data[0..23]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.data.D[0..23]
	CE   OR3374
	R    TS2736
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.i.D[0..1] = S2770[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_476.write_static_gen_6.if_545.write_16.i.D[0..1]
	CE   SB2749
	R    TS2736
    {0x0}
OR3380 = F2828 OR SD2837
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.data.D[0..23]
	<-
	SEL  F2828
	IN   E2850[0..23]
	SEL  SD2837
	IN   E2829[0..23]
    unselected out 0x0
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.data[0..23]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.data.D[0..23]
	CE   OR3380
	R    TS2810
    {0x0}
FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.i.D[0..1] = S2844[0..1]
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.i[0..1]
	<-
	CLK  glob.c16
	D    FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_474.forvar_300.for_477.write_static_gen_7.if_550.write_17.i.D[0..1]
	CE   SB2823
	R    TS2810
    {0x0}
OR3385 = TS3017 OR TS3049
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.D[0..6]
	<-
	SEL  TS3017
	IN   S3032[0..6]
	SEL  TS3049
	IN   S3062[0..6]
    unselected out 0x0
}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2[0..6]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.NF
	<-
	CLK      glob.c16
	DATA     FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.a2.D[0..6]
	PUSH     OR3385
	POP      TS2988
	RESET    GND
}
OR3388 = TS2881 OR TS2913
SELECT {
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.D[0..6]
	<-
	SEL  TS2881
	IN   S2896[0..6]
	SEL  TS2913
	IN   S2926[0..6]
    unselected out 0x0
}
QUEUEBUFFER  depth 2 {
	OUT      FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1[0..6]
	NE       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.NE
	NF       FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.NF
	<-
	CLK      glob.c16
	DATA     FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.a1.D[0..6]
	PUSH     OR3388
	POP      TS2957
	RESET    GND
}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_554.multiplext__1.case_97.select1[0]
	<-
	CLK  glob.c16
	D    E2949[0]
	CE   TS2943
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.multiplext__2.case_102.select1[0]
	<-
	CLK  glob.c16
	D    E3085[0]
	CE   TS3079
	R    GND
    {0x0}
REG
	OUT  FMOD7/uart_cpu.trailermodule_0.create_bus_interfaces_0.if_553.multiplext_0.multiplext__0.case_96.if_555.s1[0]
	<-
	CLK  glob.c16
	D    E3099[0]
	CE   TS3093
	R    GND
    {0x0}
DFF FDRSE {
	OUT      SDD3392
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3393
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3394
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3395
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3396
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3397
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3398
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3399
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3400
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3401
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3402
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3403
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3404
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3405
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3406
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3407
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3408
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3409
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3410
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3411
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
init = R
}
DFF FDRSE {
	OUT      SDD3412
	<-
	D        GND
	C        glob.c16
	CE       GND
	R        GND
	S        SSD1823
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



