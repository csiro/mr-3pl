prog.bit:	prog.edn
	ngdbuild prog
	map -detail -w -timing -ol high -pr b -cm speed prog
	par -w -ol high prog prog.ncd
	bitgen -m -w -g LCK_cycle:4 -g GTS_cycle:1 prog
