package threepl.funcs;

import threepl.exceptions.ExEx;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;

import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * An inbuilt function to deflate (compress) text.
 */
public class DeflateFunc extends InbuiltFunc implements Constant {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  test result
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();

        if (args.size() != 1)
            throw new ExEx("deflate() must have one argument", loc);

        String  s = args.getVal(0).getSingleSval(loc);
        int     ilen = s.length();
        int     wlen;
        int     olen;
        byte[]  bsi = null;
        try {
            bsi = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This should never happen.
            e.printStackTrace();
        }
        byte[]      bso = new byte[ilen];   // worst case - same size
        Deflater    d = new Deflater();
        d.setInput(bsi);
        d.finish();
        olen = d.deflate(bso);
        wlen = (olen + 3) / 4;
        Long[]  wo = new Long[wlen+3];
        
        int     j = 3;
        int     k = 3;
        long    w = 0;
        long    l;
        long    v;
        byte    b;
        CRC32   crc = new CRC32();
        
        v = ilen;
        for (int i=0 ; i<4 ; i++) {
            b = (byte)(v & 0xff);
            v >>= 8;
            crc.update(b);
        }
        v = olen;
        for (int i=0 ; i<4 ; i++) {
            b = (byte)(v & 0xff);
            v >>= 8;
            crc.update(b);
        }
        
        wo[1] = (long)ilen;             // second word is the uncompressed data length in bytes
        wo[2] = (long)olen;             // third word is the compressed data length in bytes
        for (int i=0 ; i<olen ; i++) {
            j = i % 4;
            l = bso[i] & 0xff;
            crc.update((byte)l);
            w = w | (l << (8 * j));
            if (j == 3) {
                wo[k++] = w;
                w = (long)0;
            }
        }
        if (j != 3)     // last word not filled
            wo[k] = w;
        wo[0] = crc.getValue();         // first word is a 32 bit checksum of the remainder of the block
        
        return(new Val((Object[])wo, "uint", loc));
    }
}
