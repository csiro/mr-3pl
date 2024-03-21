from distutils.core import setup, Extension
import os

instdir = os.environ['HOME'] + '/install/3pl'

module = Extension(
    'fpga_read_write',
    include_dirs=[instdir + '/include'],
    library_dirs=[instdir + '/lib/python3'],
    libraries=['z'],
    sources=['fpga_py_extension.c', 'fpga.c'],
)

setup(name='fpga_read_write', ext_modules=[module])
