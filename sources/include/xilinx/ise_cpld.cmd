# post-processing recipe for Xilinx ISE (commands), for CPLDs

ngdbuild @designName@
cpldfit -slew slow -optimize density -p @part@ @designName@.ngd
hprep6 -i @designName@
