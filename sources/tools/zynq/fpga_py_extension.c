// This includes fpga.h and must link to fpga.c
//
#include <Python.h>
#include "fpga.h"
//#include "fpga.c"

static PyObject *   py_fpga_open(PyObject *self, PyObject *args);
static PyObject *   py_fpga_close(PyObject *self, PyObject *args);
static PyObject *   py_fpga_configfile_load(PyObject *self, PyObject *args);
static PyObject *   py_fpga_configdata_load(PyObject *self, PyObject *args);   
static PyObject *   py_uio_event_wait(PyObject *self, PyObject *args);
static PyObject *   py_uio_event_count(PyObject *self, PyObject *args);
static PyObject *   py_uio_event_control(PyObject *self, PyObject *args);
static PyObject *   py_uio_event_fd(PyObject *self, PyObject *args);
static PyObject *   py_uio_event_unit(PyObject *self, PyObject *args);
static PyObject *   py_uio_dma_phys_address(PyObject *self, PyObject *args);
static PyObject *   py_uio_dma_get_word(PyObject *self, PyObject *args);
static PyObject *   py_uio_dma_put_word(PyObject *self, PyObject *args);
static PyObject *   py_fpga_burst_read(PyObject *self, PyObject *args);
static PyObject *   py_fpga_burst_write(PyObject *self, PyObject *args);

static __inline__ void
fpga_burst_read(volatile uint32_t *devaddr, uint32_t *memaddr, int dww);

static __inline__ void
fpga_burst_write(volatile uint32_t *devaddr, uint32_t *memaddr, int dww);




static PyObject *
py_fpga_open(PyObject *self, PyObject *args) {
    fpga_open();
    Py_RETURN_NONE;
}

static PyObject *
py_fpga_close(PyObject *self, PyObject *args) {
    fpga_close();
    Py_RETURN_NONE;
}

static PyObject *
py_fpga_configfile_load(PyObject *self, PyObject *args) {
    char *filename;

    if (!PyArg_ParseTuple(args, "z", &filename)) {
        printf("py_fpga_configfile_load() read argument error\n");
        exit(1);
    }
    
    fpga_configfile_load(filename);
    
    Py_RETURN_NONE;
}

static PyObject *
py_fpga_configdata_load(PyObject *self, PyObject *args) {
    uint32_t    len;
    Py_buffer   data;

    if (!PyArg_ParseTuple(args, "y*I", &data, &len)) {
        printf("py_fpga_configdata_load() read argument error\n");
        exit(1);
    }
    
    uint32_t    *p =(uint32_t *)(data.buf);
    
    fpga_configdata_load(p, len);
    
    Py_RETURN_NONE;
}

// Wait for event defined by argument.
static PyObject *
py_uio_event_wait(PyObject *self, PyObject *args) {
    // Event.
    uint32_t    n_ev;

    if (!PyArg_ParseTuple(args, "i", &n_ev)) {
        printf("py_uio_event_wait() read argument error\n");
        exit(1);
    }
    
    fpga_event_wait(n_ev);

    Py_RETURN_NONE;
}

// Enable or disable events (interrupts) according to the argument.
static PyObject *
py_uio_event_control(PyObject *self, PyObject *args) {
    uint32_t    n_ev;   // event
    uint32_t    n_c;    // 0 for disable, 1 for enable

    if (!PyArg_ParseTuple(args, "ii", &n_ev, &n_c)) {
        printf("py_uio_event_control() read argument error\n");
        exit(1);
    }
    
    fpga_event_control(n_ev, n_c);
    
    Py_RETURN_NONE;
}

static PyObject *
py_uio_event_fd(PyObject *self, PyObject *args) {
    // Event pointer.
    uint32_t    n_ev;

    if (!PyArg_ParseTuple(args, "i", &n_ev)) {
        printf("py_uio_event_fd() read argument error\n");
        exit(1);
    }
    
    return Py_BuildValue("i", events[n_ev].fd);
}

static PyObject *
py_uio_event_unit(PyObject *self, PyObject *args) {
    // Event pointer.
    uint32_t    n_ev;

    if (!PyArg_ParseTuple(args, "i", &n_ev)) {
        printf("py_uio_event_index() read argument error\n");
        exit(1);
    }
    
    return Py_BuildValue("i", events[n_ev].unit);
}

static PyObject *
py_uio_event_count(PyObject *self, PyObject *args) {
    // Event pointer.
    uint32_t    n_ev;

    if (!PyArg_ParseTuple(args, "l", &n_ev)) {
        printf("py_uio_event_count() read argument error\n");
        exit(1);
    }
    
    return Py_BuildValue("l", uio_event_count(n_ev));
}

static PyObject *
py_uio_dma_phys_address(PyObject *self, PyObject *args) {
    return Py_BuildValue("i", dma_space.phys);
}

static PyObject *
py_uio_dma_get_word(PyObject *self, PyObject *args) {
    // Buffer subscript.
    uint32_t    index;

    if (!PyArg_ParseTuple(args, "i", &index)) {
        printf("py_uio_dma_word() read argument error\n");
        exit(1);
    }

    //return Py_BuildValue("i", dma_space.base[index]);
    return Py_BuildValue("i", *(dma_space.base + index));
}

static PyObject *
py_uio_dma_put_word(PyObject *self, PyObject *args) {
    // Buffer subscript.
    uint32_t    index, w;

    if (!PyArg_ParseTuple(args, "ii", &index, &w)) {
        printf("py_uio_dma_word() read argument error\n");
        exit(1);
    }
    *(dma_space.base + index) = w;
    
    Py_RETURN_NONE;
}

static PyObject *
py_fpga_burst_read(PyObject *self, PyObject *args) {
    uint32_t    buffer[8];
    uint32_t    *base;
    uint32_t    n_mem, n_addr, n_len;
    char        *p = (char *)&buffer;;

    if (!PyArg_ParseTuple(args, "III", &n_mem, &n_addr, &n_len)) {
        printf("py_fpga_burst_read() read argument error\n");
        return(NULL);
    }

    if (n_mem == 0)
        base = reg_space.base;
    else
        base = mem_space.base;
    fpga_burst_read(base+n_addr, &buffer, n_len);

    return PyBytes_FromStringAndSize(p, n_len * 4);
}

static PyObject *
py_fpga_burst_write(PyObject *self, PyObject *args) {
    uint32_t    *base;
    uint32_t    n_mem, n_addr, n_len;
    Py_buffer   n_data;

    if (!PyArg_ParseTuple(args, "IIy*I", &n_mem, &n_addr, &n_data, &n_len)) {
        printf("py_fpga_burst_write() read argument error\n");
        return NULL;
    }
    
    uint32_t    *p =(uint32_t *)(n_data.buf);
    
    if (n_mem == 0)
        base = reg_space.base;
    else
        base = mem_space.base;
    fpga_burst_write(base+n_addr, p, n_len);
    Py_RETURN_NONE;
}

static PyMethodDef FPGA_Methods[] = {
  {"fpga_c_open", py_fpga_open, METH_VARARGS, ""},
  {"fpga_c_close", py_fpga_close, METH_VARARGS, ""},
  {"fpga_configfile_load", py_fpga_configfile_load, METH_VARARGS, ""},
  {"fpga_configdata_load", py_fpga_configdata_load, METH_VARARGS, ""},
  {"fpga_burst_read", py_fpga_burst_read, METH_VARARGS, ""},
  {"fpga_burst_write", py_fpga_burst_write, METH_VARARGS, ""},
  {"uio_event_wait", py_uio_event_wait, METH_VARARGS, ""},
  {"uio_event_fd", py_uio_event_fd, METH_VARARGS, ""},
  {"uio_event_unit", py_uio_event_unit, METH_VARARGS, ""},
  {"uio_event_count", py_uio_event_count, METH_VARARGS, ""},
  {"uio_event_control", py_uio_event_control, METH_VARARGS, ""},
  {"uio_dma_phys_address", py_uio_dma_phys_address, METH_VARARGS, ""},
  {"uio_dma_get_word", py_uio_dma_get_word, METH_VARARGS, ""},
  {"uio_dma_put_word", py_uio_dma_put_word, METH_VARARGS, ""},
  {NULL, NULL, 0, NULL}
};

static struct PyModuleDef FPGAmodule = {
  PyModuleDef_HEAD_INIT,
  "fpga_read_write", // module name
  "FPGA interface",
  -1,
  FPGA_Methods
};


PyMODINIT_FUNC PyInit_fpga_read_write(void) {
  return PyModule_Create(&FPGAmodule);
};
