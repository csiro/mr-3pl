INFO2C = ~/install/bin/uart_info2c
UART_CPU_LIB = ~/install/lib
UART_CPU_INCLUDE = ~/install/include

cpu:
	$(INFO2C) prog.info
	gcc -I $(UART_CPU_INCLUDE) -o prog prog.c $(UART_CPU_LIB)/cpu_serial.o
        
fpga:	prog.edn
	ngdbuild prog
	map -detail -w -timing -ol high -pr b -cm speed prog
	par -w -ol high prog prog.ncd
	bitgen -m -w -g LCK_cycle:4 -g GTS_cycle:1 prog
	rm -df netlist.lst prog.msk prog.ptwx xlnx_auto_0.ise xlnx_auto_0_xdb/tmp/*
	rmdir -r xlnx_auto_0_xdb/tmp
	rmdir -r xlnx_auto_0_xdb

prog.edn:	prog.3pl
	3pl -rntfs prog


