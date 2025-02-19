export TEXINPUTS = .:../latex:

.SUFFIXES:
.SUFFIXES:	.tex .fig .eps .pdf

%.pdf:	%.eps
	@echo + creating $@ from $^
	@epstopdf $*.eps

%.eps:	%.fig
	@echo + creating $@ from $^
	@fig2dev -L eps $*.fig $*.eps

texs =          alist.tex \
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

figs =          and.fig \
                btrate1.fig \
                btrate2.fig \
                c_serial.fig \
                c_zynq.fig \
                clpdcm.fig \
                comm.fig \
                commdmar.fig \
                commdmaw.fig \
                commint.fig \
                commmr.fig \
                commmw.fig \
                commr.fig \
                commw.fig \
                core1.fig \
                core3.fig \
                cpu.fig \
                cram.fig \
                dcm.fig \
                dcm2.fig \
                ddrin.fig \
                ddrout.fig \
                del.fig \
                dff.fig \
                diverge.fig \
                dowhile.fig \
		dspex1.fig \
		dspex2.fig \
                e1.fig \
                e10.fig \
                e11.fig \
                e12.fig \
                e13.fig \
                e14.fig \
                e15.fig \
                e16.fig \
                e2.fig \
                e2cpo.fig \
                e2cu.fig \
                e2o.fig \
                e3.fig \
                e4.fig \
                e5.fig \
                e6.fig \
                e7.fig \
                e8.fig \
                e9a.fig \
                e9b.fig \
                ee1a.fig \
                ee1b.fig \
                ee2b.fig \
                ex3wf.fig \
                execp.fig \
                iddr1.fig \
                iddr2.fig \
                iddr3.fig \
                iloop.fig \
                inv.fig \
                iodll.fig \
                oddr.fig \
                op.fig \
                or.fig \
                pqueue.fig \
                priority.fig \
                py_serial.fig \
                py_zynq.fig \
                queue.fig \
                reg.fig \
                resync.fig \
                rram.fig \
                select.fig \
                start.fig \
                wait.fig \
                when.fig \
                while.fig \
                xor.fig

all: doc.pdf

.PHONEY: version.tex

version.tex: version.tex.template
	@echo + creating version information
	@ant -q -S versionfilter -buildfile ../../build.xml \
	    -Dinputfile=$(CURDIR)/$^ > $@

.PRECIOUS: $(texs) $(figs)

doc.pdf: version.tex $(texs) $(figs:.fig=.pdf)
	@echo + building 3pl manual pdf
	@-pdflatex doc >/dev/null
	@-makeindex -c doc.idx >/dev/null
	@-pdflatex doc >/dev/null

manual.pdf: doc.pdf
	@echo + replacing manual.pdf stored in repository
	@cp -f doc.pdf $@

clean:
	@echo + removing generated files
	@rm -f doc.ind doc.ilg doc.aux doc.out doc.idx doc.toc doc.log
	@rm -f *.eps doc.pdf version.tex

realclean: clean
	@echo + removing fig output pdfs
	@rm -f $(figs:.fig=.pdf)
