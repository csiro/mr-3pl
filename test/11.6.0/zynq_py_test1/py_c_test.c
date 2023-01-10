#include <Python.h>


#define list_r2_to_r3()	"r2", "r3"
#define list_r2_to_r4()	list_r2_to_r3(), "r4"
#define list_r2_to_r5()	list_r2_to_r4(), "r5"
#define list_r2_to_r6()	list_r2_to_r5(), "r6"
#define list_r2_to_r7()	list_r2_to_r6(), "r7"
#define list_r2_to_r8()	list_r2_to_r7(), "r8"
#define list_r2_to_r9()	list_r2_to_r8(), "r9"

#define case_burst_n(nw, lr, la, sa, da, ma, ty) \
    case nw: \
	__asm__ __volatile__ ( \
	    "@ burst "#ty" "#nw" words using ldm/stm on r2-r"#lr"\n\t" \
	    "ldm\t%"#la", {r2-r"#lr"}\n\tstm\t%"#sa", {r2-r"#lr"}" \
	    : : "r" (ma), "r" (da) : "memory", list_r2_to_r##lr() \
	); \
	break

static __inline__ void
c_fpga_burst_read(volatile uint32_t *devaddr, uint32_t *memaddr, int dww);

static __inline__ void
c_fpga_burst_write(volatile uint32_t *devaddr, uint32_t *memaddr, int dww);


int c_fpga_burst_read (int base, int addr, int len) {
    fpga_burst_read(base + addr, (uint32_t *)&buffer, len);
}

int c_fpga_burst_write (int base, int addr, int len) {
    fpga_burst_write(base + addr, (uint32_t *)&buffer, len);
}

static PyObject *py_fpga_burst_read(PyObject *self, PyObject *args) {
    char    buffer[64];
    
    // Declare three pointers
    int *n_base, *n_addr, *n_len = NULL;


    // Parse arguments - expects three integers that will be mapped
    // to n_base, n_addr and n_len
    if (!PyArg_ParseTuple(args, "iii", &n_base, &n_addr, &n_len)) {
        print(FPGA read argument error)
        sys.exit();
    }

    // Call c-function
    c_fpga_burst_read(n_base+n_addr, &buffer, n_len);


    return PyBytes_FromStringAndSize(buffer, n_len);
}

static PyObject *py_fpga_burst_write(PyObject *self, PyObject *args) {

    // Declare four pointers
    int         *n_base, *n_addr, n_data, *n_len = NULL;
    Py_buffer   buffer


    // Parse arguments - expects four integers that will be mapped to n_base, n_addr and n_len
    if (!PyArg_ParseTuple(args, "iiy*i", &n_base, &n_addr, &buffer, &n_len)) {
        return NULL;
    }
    
    buffer 

    // Call c-function
    c_fpga_burst_write(n_base, n_addr, n_len);
}

static PyMethodDef FPGA_Methods[] = {
  {"FPGA_BURST_READ", py_fpga_burst_read, METH_VARARGS, "Function for burst read from FPGA"},
  {"FPGA_BURST_WRITE", py_fpga_burst_write, METH_VARARGS, "Function for burst write to FPGA"},
  {NULL, NULL, 0, NULL}
};


static struct PyModuleDef FPGAmodule = {
  PyModuleDef_HEAD_INIT,
  "FPGA_interface", // module name
  "FPGA interface",
  -1,
  FPGA_Methods
};


PyMODINIT_FUNC PyInit_FPGAmodule(void) {
  return PyModule_Create(&FPGAmodule);
};


static __inline__ void
c_fpga_burst_read(volatile uint32_t *devaddr, uint32_t *memaddr, int dww) {
    switch (dww) {
	case 1: *memaddr = *devaddr; break;
	case_burst_n(2, 3, 1, 0, devaddr, memaddr, read);
	case_burst_n(3, 4, 1, 0, devaddr, memaddr, read);
	case_burst_n(4, 5, 1, 0, devaddr, memaddr, read);
	case_burst_n(5, 6, 1, 0, devaddr, memaddr, read);
	case_burst_n(6, 7, 1, 0, devaddr, memaddr, read);
	case_burst_n(7, 8, 1, 0, devaddr, memaddr, read);
	case_burst_n(8, 9, 1, 0, devaddr, memaddr, read);
	default: abort(); break;
    }
}

static __inline__ void
c_fpga_burst_write(volatile uint32_t *devaddr, uint32_t *memaddr, int dww) {
    switch (dww) {
	case 1: *devaddr = *memaddr; break;
	case_burst_n(2, 3, 0, 1, devaddr, memaddr, write);
	case_burst_n(3, 4, 0, 1, devaddr, memaddr, write);
	case_burst_n(4, 5, 0, 1, devaddr, memaddr, write);
	case_burst_n(5, 6, 0, 1, devaddr, memaddr, write);
	case_burst_n(6, 7, 0, 1, devaddr, memaddr, write);
	case_burst_n(7, 8, 0, 1, devaddr, memaddr, write);
	case_burst_n(8, 9, 0, 1, devaddr, memaddr, write);
	default: abort(); break;
    }
}
