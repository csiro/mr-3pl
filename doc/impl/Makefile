.SUFFIXES:
.SUFFIXES:	.tex .fig .eps .pdf

%.pdf:	%.eps
	epstopdf $*.eps

%.eps:	%.fig
	fig2dev -L eps $*.fig $*.eps

#%.tex:	%.fig
#	fig2dev -L latex $*.fig $*.tex

figures =	add.pdf \
                add_ncc.pdf \
                addsub.pdf \
                alu.pdf \
                and.pdf \
                aqbram.pdf \
                aqcram.pdf \
                assert1.pdf \
                assert2.pdf \
                assert3.pdf \
                asyncqueuebuffer.pdf \
                asyncaddr.pdf \
                asyncfullempty.pdf \
                bintogray.pdf \
                bram1.pdf \
                bram2.pdf \
                catalog.pdf \
                cram.pdf \
                diverge.pdf \
                dowhilea.pdf \
                dowhileb.pdf \
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
                elements_nets.pdf \
                execp1.pdf \
                execp2.pdf \
                execp3.pdf \
                execp4.pdf \
                execp5.pdf \
                netlist_classes.pdf \
                or.pdf \
                queuebuffer.pdf \
                priority1_4.pdf \
                priority5.pdf \
                priority6.pdf \
                qbram.pdf \
                qcram.pdf \
                resync.pdf \
                sample.pdf \
                select0.pdf \
                select1.pdf \
                starts.pdf \
                starta.pdf \
                sub.pdf \
                sub_ncc.pdf \
                TDEVar.pdf \
                TDEVarCommon.pdf \
                ubqueueex1.pdf \
                ubqueueex1_wchain.pdf \
                ubqueueex1_rchain.pdf \
                ubqueueex2.pdf \
                ubqueue.pdf \
                vshift.pdf \
                wait.pdf \
                whena.pdf \
                whenb.pdf \
                whilea.pdf \
                whileb.pdf

doc:	doc.tex $(figures)
#	python unindent.py
	pdflatex doc
#	makeindex -c xdoc.idx
	pdflatex doc
#	mv -f xdoc.pdf doc.pdf
#	rm xdoc.tex
	mv -f doc.pdf implementation.pdf
        
