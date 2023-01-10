.SUFFIXES:
.SUFFIXES:	.tex .fig .eps .pdf

%.pdf:	%.eps
	epstopdf $*.eps

%.eps:	%.fig
	fig2dev -L eps $*.fig $*.eps

#%.tex:	%.fig
#	fig2dev -L latex $*.fig $*.tex

figures =	queue.pdf \
                dcm.pdf

doc:	doc.tex $(figures)
	pdflatex doc

install:
	scp doc.pdf asgard.web.csiro.au:/srv/www/htdocs/3pl/3pltut.pdf
	cp -f doc.pdf /hsi/apps/3pl/doc/3pltut.pdf
