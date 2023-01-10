INSTALL = ~/install/3pl
INFO2C = ~/install/3pl/info2c
UART_CPU_LIB = ~/install/3pl
UART_CPU_INCLUDE = ~/install/3pl

cpu:
	$(INFO2C) prog.info
	gcc -I $(INSTALL) -o prog prog.c $(INSTALL)/cpu_serial.c
        
fpga:	prog.edn
	ngdbuild prog
	map -detail -w -timing -ol high -pr b -cm speed prog
	par -w -ol high prog prog.ncd
	bitgen -m -w -g LCK_cycle:4 -g GTS_cycle:1 prog

prog.edn:	prog.3pl
	3pl -rntfs prog
