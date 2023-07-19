# Building Implementation Document

Simply run `make` in the current directory. If the build runs all the way through, then the file `doc.pdf` will be created and automatically renamed to `implementation.pdf`, replacing the original file.

# Dependencies

- MacOS

use MacPorts to install `LaTeX` and fonts from `TexLive`. e.g.  

    port install texlive-latex texlive-latex-extra texlive-latex-recommended texlive-fonts-recommended

I also had the `2020 TexLive` distribution installed - which I don't think comes from `MacPorts`.

If you want to rebuild the `.pdf` files from the `.fig` files, you will also need to install `fig2dev` e.g.

    port install fig2dev

- Linux

blah blah
