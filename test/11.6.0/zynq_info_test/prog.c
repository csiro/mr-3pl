#include <stdio.h>
#include <setjmp.h>
#include <zlib.h>
#include "/root/install/3pl/fpga.h"
#include "prog.h"
    
int fetch_info (unsigned char ** info);
int     uncompress_info (
            unsigned char * dest,   // destination buffer
            int             osize,  // destination buffer size
            unsigned char * src,    // source buffer
            int             isize   // source buffer size
        );


int main (int argc, const char *argv[]) {
    int             i, n;
    unsigned char * info;

    fpga_open();
    sleep(1.0);
        
    printf("s = %d\n", fpga_SR_read());
    /*
    n = fetch_info(&info);
    if (n <= 0)
        exit(1);
    
    char    c;
    for (i=0 ; i<n ; i++) {
        c = info[i];
        putchar(c);
    }

    free(info);*/
    fpga_close();
    exit(0);

}

int fetch_info (unsigned char ** info) {
    int             i;
    int             n;
    int             e;
    unsigned long * buf;
    unsigned char * ubuf;
    unsigned long * bufp;
    unsigned long   ulen;
    
    printf("initialise\n");
    fflush(stdout);
    sleep(1.0);
    fpga_inforeset_write(); // initialise info read
    printf("read checksum\n");
    fflush(stdout);
    sleep(1.0);
    n = fpga_infoword_read(); // checksum
    printf("checksum - %d\n", n);
    fflush(stdout);
    sleep(1.0);
    printf("read compressed length\n");
    n = fpga_infoword_read(); // number of bytes of compressed data
    printf("compressed length - %d\n", n);
    printf("read uncompressed length\n");
    n = fpga_infoword_read(); // number of bytes of uncompressed data
    printf("uncompressed length - %d\n", n);
    ulen = n * 10;              // enough room for uncompressed data (a guess)
    
    buf = malloc(n+3);          // enough room for compressed data, 32-bit packed
    ubuf = malloc(ulen);
    bufp = buf;
    for (i=0 ; i<n ; i+=4)
        *bufp++ = fpga_infoword_read();
    printf("uncompress\n");
    e = uncompress(ubuf, &ulen, ((Bytef *)buf), n);
    if (e != Z_OK) {
        printf("info uncompress error %d\n", e);
        return(-1);
    }
    
    /*
      * This can also be used.
      *
    e = uncompress_info(ubuf, ulen, (unsigned char *)buf, n);
    if (e < 0) {
        printf("info uncompress error %d\n", -e);
        return(-1);
    }
    ulen= e;
    */
    
    free(buf);
    *info = realloc(ubuf, ulen); // eliminate unused trailing buffer space
    return(ulen);
}

// Uncompress the information data.
int
uncompress_info (
    unsigned char * dest,   // destination buffer
    int             osize,  // destination buffer size
    unsigned char * src,    // source buffer
    int             isize   // source buffer size
) {
    z_stream    z;
    int         r;
    int         header_len = 2;

    z.zalloc = Z_NULL;
    z.zfree = Z_NULL;
    z.opaque = 0;
    z.next_in = src + header_len;       // Skip 2 header bytes (ox78, ox9C)
    z.avail_in = isize - header_len;    //
    z.next_out = dest;
    z.avail_out = osize;
    r = inflateInit2(&z, -MAX_WBITS);
    if (r != Z_OK)
        return(-3);
    r = inflate(&z, Z_FINISH);
    if (r != Z_OK && r != Z_STREAM_END)
        return(-4);
    return(osize - z.avail_out);
}
