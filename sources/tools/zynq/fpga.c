#include <stdio.h>
#include <fcntl.h>
#include <sys/types.h>
#include "fpga.h"
#include <sys/stat.h>
#include <zlib.h>


fpga_space_info_t   reg_space;
fpga_space_info_t   mem_space;
fpga_space_info_t   dma_space;

fpga_event_info_t events[FPGA_NEVENTS];


void                fpga_configdev_open(void);
void                fpga_configdev_write(uint8_t *buffer, size_t len);
void                fpga_configdev_close(void);



void
fpga_open (void) {
    reg_space = uio_map_space(REG_UIO_NAME);
    mem_space = uio_map_space(MEM_UIO_NAME);
    dma_space = uio_map_space(DMA_UIO_NAME);
    for (int i=0 ; i<FPGA_NEVENTS ; i++)
        uio_map_event(i);
}

void
fpga_close (void) {
    uio_unmap_space(reg_space);
    uio_unmap_space(mem_space);
    uio_unmap_space(dma_space);
    for (int i=0 ; i<FPGA_NEVENTS ; i++)
        uio_unmap_event(i);
}

// Wait for event defined by argument.
void
fpga_event_wait(uint32_t event_index) {
    // wait and acknowledge event by reading it
    uint32_t enumber;
    if (read(events[event_index].fd, (void *)&enumber, 4) < 0) {
	fprintf(stderr, "read(%d): %s\n", events[event_index].fd, strerror(errno));
	exit(1);
    }
    
    // Get difference from previous event number to determine how many
    // events we have received at this wait, allowing for wraparound.
    if (enumber > events[event_index].enumber)
        events[event_index].events = enumber - events[event_index].enumber;
    else
        events[event_index].events = (events[event_index].enumber ^ 0xffffffff) + enumber + 1;
    events[event_index].enumber = enumber;
}

// Enable or disable events (interrupts) according to the argument.
void
fpga_event_control(uint32_t event_index, uint32_t c) {
    if (write(events[event_index].fd, &c, 4) < 0) {
	fprintf(stderr, "read(%d): %s\n", events[event_index].fd, strerror(errno));
	exit(1);
    }
}

// Return the event file descriptor.
int
uio_event_fd(uint32_t event_index) {
    return(events[event_index].fd);
}

// Return the number of events for this interrupt.
int
uio_event_count(uint32_t event_index) {
    return(events[event_index].events);
}

// Return the DMA physical address.
uintptr_t
uio_dma_phys_address() {
    return(dma_space.phys);
}

// mmap the UIO device corresponding to name into user process space.
// Return the details (fd, base, etc) into the space structure
// FIXME this function assumes only device map[0] is used!
fpga_space_info_t
uio_map_space(char *name) {
    char    fname[50];
    char    buffer[20];
    FILE    *f;
    
    // find the UIO device unit number for the name
    unsigned            unit = uio_find(name);
    fpga_space_info_t   new;

    strcpy(new.name, name);
    sprintf(new.dev, "/dev/uio%u", unit);

    // get the size of the space from /sys
    sprintf(fname, "/sys/class/uio/uio%u/maps/map0/size", unit);
    f = fopen(fname, "r");
    fread(&buffer, 30, 1, f);
    fclose(f);
    if (sscanf(buffer, "%zx", &new.size) != 1) {
	fprintf(stderr, "%s: Can't read size\n", buffer);
	exit(1);
    }
    if (new.size == 0) {
	fprintf(stderr, "%s: device has zero size\n", new.dev);
	exit(1);
    }

    sprintf(fname, "/sys/class/uio/uio%u/maps/map0/addr", unit);
    f = fopen(fname, "r");
    fread(&buffer, 30, 1, f);
    fclose(f);
    if (sscanf(buffer, "%" SCNxPTR "", &new.phys) != 1) {
	fprintf(stderr, "%s: %s: Can't interpret physical addr\n", new.dev, buffer);
	exit(1);
    }
    //printf("map - name = %s\n", name);
    //printf("map - addr = %x\n", new.phys);
    //printf("map - size = %x\n", new.size);

    // open the UIO device
    if ((new.fd = open(new.dev, O_RDWR)) < 0) {
        fprintf(stderr, "%s: open(): %s\n", new.dev, strerror(errno));
        exit(1);
    }

    // map to process memory
    new.base = mmap(
	NULL, new.size, PROT_READ|PROT_WRITE, MAP_SHARED, new.fd, 0
    );
    if (new.base == MAP_FAILED) {
        fprintf(stderr, "%s: mmap(): %s\n", new.dev, strerror(errno));
        close(new.fd);
        exit(1);
    }
    //printf("map - base = %x\n\n", new.base);
    
    // return results
    return new;
}

void
uio_unmap_space(fpga_space_info_t space) {
    munmap((void *)space.base, space.size);
    close(space.fd);
}

// open the UIO device corresponding to name
// Return the details (fd, dev) into the event structure
void
uio_map_event(int index) {
    char                name[32];
    fpga_event_info_t   new;
    char                edev[64];
    FILE                *efp;
    int                 evn = 0;
    int                 n;

    sprintf(name, "event%d", index);
    
    // find the UIO device unit number for the name
    new.unit = uio_find(name);   
    strcpy(new.name, name);

    // open the UIO device
    sprintf(new.dev, "/dev/uio%u", new.unit);
    new.fd = open(new.dev, O_RDWR);
    if (new.fd < 0) {
        fprintf(stderr, "%s: open(): %s\n", new.dev, strerror(errno));
        exit(1);
    }
    /* 
     * Code to attempt to get the last event number.
     * read() fails with 'resource temporarily unavailable'
     */
    sprintf(edev, "/sys/class/uio/uio%u/event", new.unit);
    efp = fopen(edev, "r");
    if (efp == NULL) {
        fprintf(stderr, "%s: open event number: %s\n", edev, strerror(errno));
        exit(1);
    }
    n = fscanf(efp, "%d", &evn);
    if (n <= 0) {
        fprintf(stderr, "%s: read event number: %s\n", edev, strerror(errno));
        exit(1);
    }
    fclose(efp);
    
    new.enumber = evn;
    new.events = 0;
    //printf("index %d evn %d\n", index, evn);
    
    events[index] = new;
}

void
uio_unmap_event(int index) {
    close(events[index].fd);
}

// Search all UIO devices and find one with the given name.
// Return its unit number.
int
uio_find(char *name) {
    DIR *dir;
    unsigned unit = 0;
    
    if (! (dir = opendir("/sys/class/uio"))) {
	fprintf(stderr, "/sys/class/uio: opendir(): %s\n", strerror(errno));
	exit(1);
    }
    
    while (1) {
	struct dirent *entry;

	// get next entry in list, if any
	if (! (entry = readdir(dir))) {
	    if (errno) 
		fprintf(stderr, "/sys/class/uio: readdir(): %s\n", strerror(errno));
	    else
		fprintf(stderr, "%s: No such UIO name\n", name);
	    closedir(dir);
	    exit(1);
	}

	// ignore . and .. entries
	if (
	    (strcmp(entry->d_name, ".") == 0) ||
	    (strcmp(entry->d_name, "..") == 0)
	)
	    continue;

	// get the number of the end of the name
	if (sscanf(entry->d_name, "uio%u", &unit) != 1) {
	    fprintf(stderr, "%s: Can't read unit number\n", entry->d_name);
	    exit(1);
	}

	// open the file to get the name associated with this number
	char    fname[30];
        char    buffer[30];
        sprintf(fname, "/sys/class/uio/uio%u/name", unit);
        
        FILE *f = fopen(fname, "r");
        fread(&buffer, 30, 1, f);
        fclose(f);
        
	// test for match (restrict length to ignore \n)
	if (strncmp(name, buffer, strlen(name)) == 0)
	    break;
    }

    return unit;
}











void
fpga_configfile_load(char *filename) {
    uint8_t     *buffer;
    FILE        *f;
    size_t      flen;
    uint8_t     *p;
    int         n;
    
    f = fopen(filename, "r");
    if (f == NULL) {
        fprintf(stderr, "cannot open configuration file '%s'\n", filename);
        exit(1);
    }
    fseek(f, 0, 2);
    flen = ftell(f);
    rewind(f);
    
    buffer = malloc(flen);
    p = buffer;
    
    do {
        n = fread(p, 1, flen, f);
        if (ferror(f)) {
            fprintf(stderr, "read error on configuration file '%s'\n", filename);
            exit(1);
        }
        p += n;
    } while (n != 0);
    
    fpga_configdata_load(buffer, flen);
    
    free(buffer);
}







//
// Load the FPGA represented by @p fpga with the configuration pointed to by
// 'data', which has length 'len'. The data is usually in a static array
// generated from the bin2c program from the configuration file, and
// compiled into the program. However, the program may also have loaded the
// data dynamically from a file.
//
// The configuration data may have been compressed using gzip.
// The latter case is recognised automatically from the gzip
// header bytes in the data. An error is returned if the data does not
// uncompress correctly. In that case, the FPGA will probably be left
// unconfigured and in a reset state.
//
// If an error is encoutered by fpga_configdata_load() when
// loading the FPGA device, including failure to assert the "done" status.
// In that case, the FPGA will be left unconfigured and in reset state.
// The cause is usually that the file is for a different FPGA part.
//
// This function also captures configuration metadata and strips off the
// *info file*, if appended to the configuration data, and attaches these
// to the @p fpga structure. The *info file* is also cached for other
// processes. See the general comments above for more information.
//
// This function is typically used where the user wants a self-contained
// binary program for a specific board that loads and uses the FPGA,
// or want complete control over the loading process. This is unusual and
// the function fpga_configfile_load() is more commonly used to load the FPGA.
//
void
fpga_configdata_load(uint8_t *data, size_t len) {
    // look for gzip compression header in data
    if (! ((data[0] == 0x1f) && (data[1] == 0x8b))) {

	// copy uncompressed data directly
	fpga_configdev_open();
	fpga_configdev_write(data, len);
	fpga_configdev_close();
        
	return;
    }

    // otherwise, set up decompression
    unsigned char buffer[BUFSIZ];
    z_stream    z;
    
    z.zalloc = Z_NULL;
    z.zfree = Z_NULL;
    z.opaque = Z_NULL;
    z.next_in = (unsigned char *)data;
    z.avail_in = (unsigned)len;
    z.next_out = buffer;
    z.avail_out = BUFSIZ;

    // the extra 32 makes it read the zlib/gzip header
    int r = inflateInit2(&z, MAX_WBITS + 32);
    if (r != Z_OK) {
	fprintf(stderr, "inflateInit2: %s\n", z.msg);
	exit(1);
    }

    // set up FPGA to load
    fpga_configdev_open();

    // decompress block by block
    while (r != Z_STREAM_END) {
	z.next_out = buffer;
	z.avail_out = BUFSIZ;
	r = inflate(&z, Z_SYNC_FLUSH);

	if ((r == Z_OK) || (r == Z_STREAM_END)) {
	    fpga_configdev_write(buffer, BUFSIZ - z.avail_out);
	    // FIXME leak??
	    continue;
	}

	fprintf(stderr, "inflate: %s\n", z.msg);
	inflateEnd(&z);
	fpga_configdev_close();
	exit(1);
    }

    // check FPGA is loaded 
    fpga_configdev_close();

    // FIXME do something with gzip header info?

    // clean up
    if (inflateEnd(&z) != Z_OK)
	fprintf(stderr, "inflateEnd: %s\n", z.msg);
}

#define FPGA_XDEVCFG_DEVICE "/dev/xdevcfg"
#define FPGA_XDEVCFG_DONE_FILE "/sys/class/xdevcfg/xdevcfg/device/prog_done"


char    *fpga_configdev;
int     fpga_configfd;

// open the configuration device
void
fpga_configdev_open(void) {
    fpga_configdev = FPGA_XDEVCFG_DEVICE;
    if ((fpga_configfd = open(fpga_configdev, O_RDWR)) < 0) {
	fprintf(stderr, "%s: open(): %s\n", fpga_configdev, strerror(errno));
	exit(1);
    }
}

// write a block to the device -- called repeatedly
void
fpga_configdev_write(uint8_t *buffer, size_t len) {
    if (write(fpga_configfd, buffer, len) != len) {
	fprintf(stderr, "%s: write(): %s\n", fpga_configdev, strerror(errno));
	close(fpga_configfd);
	exit(1);
    }
}


// close the device
void
fpga_configdev_close(void) {
    // close configuration device
    int configfd = fpga_configfd;
    if (close(configfd)) {
	fprintf(stderr, "%s: close(): %s\n", fpga_configdev, strerror(errno));
        exit(1);
    }

    // check for DONE
    char buffer[32] = "0";
    FILE    *f;
    
    f = fopen(FPGA_XDEVCFG_DONE_FILE, "r");
    fread(buffer, 1, 1, f);
    fclose(f);
    if (*buffer == '0') {
	fprintf(stderr, "%s: No DONE: wrong or bad config?", fpga_configdev);
	exit(1);
    }
}

