// Readback is initialised by writing to
// address 1 and 32-bit packed .info data is then read by successive 32-bit
// reads from address 1. The first read returns a 32 bit CRC-32 checksum
// for the remainder of the block,\. The second read returns the
// uncompressed data size in bytes and the third read returns the
// compressed data size in bytes. Reads beyond the data length will return
// zeros.

int fetch_info (unsigned char ** info) {
   int             i;
   int             n;
   int             e;
   unsigned long * buf;
   unsigned char * ubuf;
   unsigned long * bufp;
   unsigned long   ulen;

   fpga_reg_write(fpga, 1, 0);
   n = fpga_reg_read(fpga, 1); // number of bytes of compressed data
   ulen = n * 10;              // enough room for uncompressed data (a guess)

   buf = malloc(n+3);          // enough room for compressed data, 32-bit packed
   ubuf = malloc(ulen);
   bufp = buf;
   for (i=0 ; i<n ; i+=4)
       *bufp++ = fpga_reg_read(fpga, 1);

   e = uncompress(ubuf, &ulen, ((Bytef *)buf), n);
   if (e != Z_OK) {
       printf("info uncompress error %d\n", e);
       return(-1);
   }

   free(buf);
   *info = realloc(ubuf, ulen); // eliminate unused trailing buffer space
   return(ulen);
}
