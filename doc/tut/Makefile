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
    doc.tex

figs = \
    queue.fig \
    dcm.fig

all: doc.pdf

.PRECIOUS: $(figs:.fig=.pdf)

doc.pdf: $(texs) $(figs:.fig=.pdf)
	@echo + building 3pl tutorial pdf - as $@
	@-pdflatex doc >/dev/null
	@-pdflatex doc >/dev/null

tutorial.pdf: doc.pdf
	@echo + replacing $@ stored in repository
	@cp -f $^ $@

clean:
	@echo + removing generated files
	@rm -f doc.log doc.aux doc.atc
	@rm -f *.eps doc.pdf

realclean: clean
	@echo + removing fig output pdfs
	@rm -f $(figs:.fig=.pdf)
