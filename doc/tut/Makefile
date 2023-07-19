export TEXINPUTS = .:../latex:

.SUFFIXES:
.SUFFIXES:	.tex .fig .eps .pdf

%.pdf:	%.eps
	epstopdf $*.eps

%.eps:	%.fig
	fig2dev -L eps $*.fig $*.eps

figures =	queue.pdf \
                dcm.pdf

.PRECIOUS: $(figures)

tutorial.pdf:	doc.tex $(figures)
	pdflatex doc
	pdflatex doc
	mv -f doc.pdf $@

clean:
	rm -f tutorial.pdf doc.log doc.aux doc.atc $(figures)
