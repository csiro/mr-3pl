# post-processing recipe for Xilinx ISE (commands), for FPGAs

ngdbuild @designName@
map -detail -w -timing -ol high -pr b @designName@
par -w -ol high @designName@ @designName@.ncd
rm -f @designName@.twr
if_constraints_unmet trce -e 50 -l 5 -u 100 @designName@
bitgen -m -w -g LCK_cycle:4 -g GTS_cycle:1 @designName@
