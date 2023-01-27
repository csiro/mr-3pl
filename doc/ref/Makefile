TOP = ../..
include $(TOP)/common.mk

all: doc.pdf

.SUFFIXES:
.SUFFIXES:	.tex .fig .eps .pdf

%.pdf:	%.eps
	@echo + creating $@ from $^
	@epstopdf $*.eps

%.eps:	%.fig
	@echo + creating $@ from $^
	@fig2dev -L eps $*.fig $*.eps

#%.tex:	%.fig
#	fig2dev -L latex $*.fig $*.tex

texts =         alist.tex \
                boards_avnet_sp3a.tex \
                boards_microzed.tex \
                boards_ml505.tex \
                boards_papilio.tex \
                boards_v4fx.tex \
                boards_zybo.tex \
                building_3pl.tex \
                CPLDs.tex \
                capture.tex \
                clock_attributes.tex \
                cmemory_attributes.tex \
                code_examples.tex \
                code_structure.tex \
                coding_rec.tex \
                compiler_versions.tex \
                constraints_etc.tex \
                cpu.tex \
                cpu_rm.tex \
                cpu_rq.tex \
                cpu_rv.tex \
                cpu_wm.tex \
                cpu_wq.tex \
                cpu_ws.tex \
                ctypes.tex \
                doc.tex \
                EDIF_cores.tex \
                FPGA_hardware.tex \
                file_system.tex \
                float.tex \
                glist.tex \
                immediate_attributes.tex \
                immediate_statements.tex \
                inbuilt_functions.tex \
                inbuilt_modules.tex \
                inbuilt_procedures.tex \
                input_attributes.tex \
                intermediate_code.tex \
                introduction.tex \
                iopins_attributes.tex \
                libraries.tex \
                jsontype.tex \
                limitations.tex \
                memory_etc.tex \
                output_attributes.tex \
                overview.tex \
                platform_module_files.tex \
                priority_attributes.tex \
                qdr2_ram.tex \
                queue_attributes.tex \
                regular_expressions.tex \
                rmemory_attributes.tex \
                running_compiler.tex \
                scan.tex \
                selectvalue_attributes.tex \
                source_structure.tex \
                spi.tex \
                static_attributes.tex \
                stdlib.tex \
                syntax.tex \
                target_statements.tex \
                tools.tex \
                UART_CPU.tex \
                uart.tex \
                uarts.tex \
                value_attributes.tex \
		variables_etc.tex \
                xilinx_axi.tex \
                xilinx_clocks.tex \
                xilinx_common.tex \
                xilinx_dsp.tex \
                xilinx_gtp_dual.tex \
                xilinx_io.tex \
                xilinx_mgt.tex \
                xilinx_postprocess.tex \
                xilinx_ppc405.tex \
                xilinx_ppclib.tex \
                zbt_ram.tex \
                zynq_CPU_comm.tex \
		zynq_device_tree.tex

figures =       and.pdf \
                btrate1.pdf \
                btrate2.pdf \
                c_serial.pdf \
                c_zynq.pdf \
                clpdcm.pdf \
                comm.pdf \
                commdmar.pdf \
                commdmaw.pdf \
                commint.pdf \
                commmr.pdf \
                commmw.pdf \
                commr.pdf \
                commw.pdf \
                core1.pdf \
                core3.pdf \
                cpu.pdf \
                cram.pdf \
                dcm.pdf \
                dcm2.pdf \
                ddrin.pdf \
                ddrout.pdf \
                del.pdf \
                dff.pdf \
                diverge.pdf \
                dowhile.pdf \
		dspex1.pdf \
		dspex2.pdf \
                e1.pdf \
                e10.pdf \
                e11.pdf \
                e12.pdf \
                e13.pdf \
                e14.pdf \
                e15.pdf \
                e16.pdf \
                e2.pdf \
                e2cpo.pdf \
                e2cu.pdf \
                e2o.pdf \
                e3.pdf \
                e4.pdf \
                e5.pdf \
                e6.pdf \
                e7.pdf \
                e8.pdf \
                e9a.pdf \
                e9b.pdf \
                ee1a.pdf \
                ee1b.pdf \
                ee2b.pdf \
                ex3wf.pdf \
                execp.pdf \
                iddr1.pdf \
                iddr2.pdf \
                iddr3.pdf \
                iloop.pdf \
                inv.pdf \
                iodll.pdf \
                oddr.pdf \
                op.pdf \
                or.pdf \
                pqueue.pdf \
                priority.pdf \
                py_serial.pdf \
                py_zynq.pdf \
                queue.pdf \
                reg.pdf \
                resync.pdf \
                rram.pdf \
                select.pdf \
                start.pdf \
                wait.pdf \
                when.pdf \
                while.pdf \
                xor.pdf

.PHONEY: version.tex

version.tex: version.tex.template
	@echo + creating version information
	@sed \
            -e $qs?@VERSION@?$(VERSION)$(VERSIONMOD)?$q \
            -e $qs?@SVNVERSION@?$(SVNVERSION)?$q \
            -e $qs?@SVNAUTHOR@?$(SVNTOPAUTHOR)?$q \
            -e $qs?@SVNREV@?$(SVNTOPREV)?$q \
            -e $qs?@SVNDATE@?$(SVNTOPDATE)?$q\
            -e $qs?@BUILDDATE@?$(BUILDDATE)?$q \
            -e $qs?@BUILTBY@?$(BUILTBY)?$q \
		< version.tex.template > version.tex

doc.pdf:	version.tex $(texts) $(figures)
	@echo + building 3pl manual pdf
	@-pdflatex doc
	@-makeindex -c doc.idx
	@-pdflatex doc
	@rm version.tex
	mv -f doc.pdf manual.pdf
        
build: all
	@echo + copying 3pl manual pdf to doc dir
	@mkdir -p $(BUILDDIR)/doc
	@cp -f doc.pdf $(BUILDDIR)/doc/3plmanual.pdf

.PHONY: install
install: build

.PHONY: winstall
winstall: all
	scp doc.pdf asgard.web.csiro.au:/data/www/htdocs/3pl/3plmanual.pdf
