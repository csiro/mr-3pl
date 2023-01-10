import sys
import os
import re
from stat import *

def change (f):
    print("file " + f)
    ifile = open(f, 'r')
    ofile = open('temp', 'w')
    
    indent = 0
    for line in ifile:
        if '\\begin{lstlisting}' in line:
            indent = line.find('\\begin{lstlisting}')
            line = re.sub('language', 'gobble=' + str(indent) + ', language', line)
            ofile.write(line)
        elif '\end{lstlisting}' in line:
            indent = 0
            ofile.write(line)
        else:
            ofile.write(line)
    
    os.remove(f)
    os.rename('temp', f)
    ofile.close()
    return
    

# Traverse a directory selecting file names ending with ".tex".
# Call procedure 'change()'
def traverse (d):
    for filename in os.listdir(d):
        if filename.endswith(".tex"):
            change(filename)
    
# Main entry.
# traverse the current directory.
#
root = os.getcwd()
traverse(root)



