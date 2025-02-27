export TEXINPUTS = .:../latex:

.SUFFIXES:
.SUFFIXES: .tex .fig .eps .pdf

%.pdf: %.eps
	@echo + creating $@ from $^
	@epstopdf $*.eps

%.eps: %.fig
	@echo + creating $@ from $^
	@fig2dev -L eps $*.fig $*.eps

texs = \
    Element.tex \
    GenFunctions.tex \
    TDE.tex \
    TDECode.tex \
    TDEList.tex \
    TDEVar.tex \
    TDEVarList.tex \
    WordSpec.tex \
    XElements.tex \
    XFunctions.tex \
    XTDECode.tex \
    doc.tex \
    inbuilt.tex \
    net.tex \
    netnet.tex

figs = \
    add.fig \
    add_ncc.fig \
    addsub.fig \
    alu.fig \
    and.fig \
    aqbram.fig \
    aqcram.fig \
    assert1.fig \
    assert2.fig \
    assert3.fig \
    asyncqueuebuffer.fig \
    asyncaddr.fig \
    asyncfullempty.fig \
    bintogray.fig \
    bram1.fig \
    bram2.fig \
    catalog.fig \
    cram.fig \
    diverge.fig \
    dowhilea.fig \
    dowhileb.fig \
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
    elements_nets.fig \
    execp1.fig \
    execp2.fig \
    execp3.fig \
    execp4.fig \
    execp5.fig \
    netlist_classes.fig \
    or.fig \
    queuebuffer.fig \
    priority1_4.fig \
    priority5.fig \
    priority6.fig \
    qbram.fig \
    qcram.fig \
    resync.fig \
    sample.fig \
    select0.fig \
    select1.fig \
    starts.fig \
    starta.fig \
    sub.fig \
    sub_ncc.fig \
    TDEVar.fig \
    TDEVarCommon.fig \
    ubqueueex1.fig \
    ubqueueex1_wchain.fig \
    ubqueueex1_rchain.fig \
    ubqueueex2.fig \
    ubqueue.fig \
    vshift.fig \
    wait.fig \
    whena.fig \
    whenb.fig \
    whilea.fig \
    whileb.fig

all: doc.pdf

.PRECIOUS: $(figs:.fig=.pdf)

doc.pdf: $(texs) $(figs:.fig=.pdf)
	@echo + building 3pl implementation pdf - as $@
	@-pdflatex doc >/dev/null
	@-pdflatex doc >/dev/null

implementation.pdf: doc.pdf
	@echo + replacing implementation.pdf stored in repository
	@cp -f $^ $@

clean:
	@echo + removing generated files
	@rm -f doc.aux doc.out doc.log doc.toc missfont.log
	@rm -f *.eps doc.pdf

realclean: clean
	@echo + removing fig output pdfs
	@rm -f $(figs:.fig=.pdf)
