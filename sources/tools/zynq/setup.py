from distutils.core import setup, Extension
import os

def main():
    module = Extension('fpga_read_write',
        include_dirs = [os.environ['HOME'] + '/install/3pl'],
        library_dirs = ['/lib'],
        libraries = ['z'],
        sources = ['fpga_py_extension.c', 'fpga.c'])

    setup (name = 'fpga_read_write',
           ext_modules = [module])

if (__name__ == "__main__"):
  main()
