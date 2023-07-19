export TEXINPUTS = .:../latex:

doc:	install.pdf

install.pdf: doc.tex
	pdflatex doc
	pdflatex doc
	mv -f doc.pdf $@

clean:
	rm -f install.pdf doc.aux doc.log
