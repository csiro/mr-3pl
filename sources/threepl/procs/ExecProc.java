package threepl.procs;

import static threepl.ThreePL.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import threepl.exceptions.ExEx;
import threepl.exec.Ref;
import threepl.exec.Val;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt procedure to execute a command.
 */
public class ExecProc extends InbuiltProc implements Constant {

    /**
     * Construct the inbuilt procedure exec().
     * This is called exactly once by method threepl.exec.InbuiltProcs().
     */
    public ExecProc () {
        allowed_as_param = false;
        check_null_input_args = false;
        check_null_output_args = false;
        target_inline = false;
        ipnames.put("cmd", 0);
        ipnames.put("env", 1);
        ipnames.put("dir", 2);
        ipnames.put("in", 3);
        opnames.put("stat", 0);
        opnames.put("out", 1);
        opnames.put("err", 2);
    }

    /**
     * Execute the procedure exec(). This does not generate executable code.
     * @param   inargs is a list of input argument tree nodes
     * @param   outargs is a list of output argument tree nodes
     * @param   toplevel is true if we are at the module, procedure or function
     *          level
     */
    public void execute (
        NodeList    inargs, 
        NodeList    outargs, 
        boolean     toplevel
    ) {
        SrcLoc      loc = inargs.getCallLoc();
        if ((inargs.size() < 1) || (inargs.size() > 4))
            throw new ExEx("exec() must have one to four input arguments", loc);
        if (outargs.size() > 3)
            throw new ExEx("exec() cannot have more than three output arguments", loc);
        
        Ref     statref = null;
        Ref     osref = null;
        Ref     erref = null;
        Val     isval;
        String  is_string = null;

        if (outargs.size() > 0) {
            // optional return status
            statref = outargs.getRef(0, "exec() - ");
            if (statref != null) {
                if (statref.getMode() != Mode.IMMEDIATE)
                     throw new ExEx("exec() - 1st output argument not immediate", loc);
                if ((statref.getPrimType() != Ptype.INT) && (statref.getPrimType() != Ptype.UINT))
                    throw new ExEx("exec() - 1st output argument not type \"int\" or \"uint\"", loc);
            }
        }

        if (outargs.size() > 1) {
            // optional output string
            osref = outargs.getRef(1, "exec() - ");
            if (osref != null) {
                if (osref.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("exec() - 2nd output argument not immediate", loc);
                if (osref.getPrimType() != Ptype.STR)
                    throw new ExEx("exec() - 2nd output argument not type \"str\"", loc);
            }
        }

        if (outargs.size() > 2) {
            // optional error string
            erref = outargs.getRef(2, "exec() - ");
            if (erref != null) {
                if (erref.getMode() != Mode.IMMEDIATE)
                    throw new ExEx("exec() - 3rd output argument not immediate", loc);
                if (erref.getPrimType() != Ptype.STR)
                    throw new ExEx("exec() - 3rd output argument not type \"str\"", loc);
            }
        }

        Val         cval = inargs.getVal(0);
        if (cval.getMode() != Mode.IMMEDIATE)
             throw new ExEx("exec() - 1st input argument not immediate", loc);
        // this line gave a NullPointerException if a non-array was passed, as
        // getDimDes() returned null in that case. I'm not sure if it should be
        // fixed here, or in getDimDes(), so I did it here
        if ((cval.getDimDes() == null) || (cval.getDimDes().length != 1))
            throw new ExEx("exec() - 1st input argument not type \"[]str\"", loc);
        if (cval.getCheckType().getPrimType() != Ptype.STR)
            throw new ExEx("exec() - 1st input argument not type \"[]str\"", loc);
        int         n = cval.numWords();
        String[]    cmdarray = new String[n];
        for (int i=0 ; i<n ; i++)
            cmdarray[i] = (String)cval.getVal(i);
        
        Val         eval = null;
        Val         dval = null;
        String[]    env = null;
        File        dir = null;
        
        if ((inargs.size() > 1) && ((eval = inargs.getVal(1)) != null)) {
            // optional environment variable map
            if (eval.getMode() != Mode.IMMEDIATE)
                 throw new ExEx("exec() - 2nd input argument not immediate", loc);
            if (eval.getCheckType().getPrimType() != Ptype.MAP)
                throw new ExEx("exec() - 2nd input argument not type \"map\"", loc);
            TreeMap<String,Val>         map = eval.getSingleMAPval(loc);
            Set<Map.Entry<String,Val>>  map_set = map.entrySet();
            int                         map_entries = map_set.size();
            int                         i = 0;
            
            env = new String[map_entries];
            for (Map.Entry<String,Val> me : map_set) {
                String  mkey = me.getKey();
                Val     mval = me.getValue();
                switch (mval.getPrimType()) {
                case INT:
                case UINT:
                    env[i++] = mkey + "=" + mval.getSingleIval(loc);
                    break;
                case STR:
                    env[i++] = mkey + "=" + mval.getSingleSval(loc);
                    break;
                case FLOAT:
                    env[i++] = mkey + "=" + mval.getSingleFval(loc);
                    break;
                default:
                    throw new ExEx("exec() - 2nd input argument (type \"map\") invalid entry, key '" +
                                mkey + "' value type " + mval.getPrimType().typename(), loc);
                }
            }
        }
        if ((inargs.size() > 2) && ((dval = inargs.getVal(2)) != null)) {
            // optional working directory string
            if (dval.getMode() != Mode.IMMEDIATE)
                 throw new ExEx("exec() - 3rd input argument not immediate", loc);
            if (dval.getPrimType() != Ptype.STR)
                throw new ExEx("exec() - 3rd input argument not type \"str\"", loc);
            String  name = dval.getSingleSval(loc);
            if (name.length() != 0)
                dir = new File(name);
        }
        if ((inargs.size() > 3) && ((isval = inargs.getVal(3)) != null)) {
            // optional input string
            if (isval.getMode() != Mode.IMMEDIATE)
                throw new ExEx("exec() - 4th output argument not immediate", loc);
            if (isval.getPrimType() != Ptype.STR)
                throw new ExEx("exec() - 4th output argument not type \"str\"", loc);
            is_string = isval.getSingleSval(loc);
        }

        try {
            Process         proc = java.lang.Runtime.getRuntime().exec(cmdarray, env, dir);
            OutputStream    os = proc.getOutputStream();
            InputStream     is = proc.getInputStream();
            InputStream     es = proc.getErrorStream();
            boolean         proc_done;
            StringBuffer    ib = new StringBuffer();
            StringBuffer    eb = new StringBuffer();
            int             bytes;
            byte[]          ba;
            
            // write input to the process "stdin"
            if (is_string != null) {
                ba = is_string.getBytes();
                for (int i=0 ; i<ba.length ; i++)
                    os.write(ba[i]);
            }
            os.close();

            do {
                // wait with a 1s timeout, so we can read stdout/stderr
                // otherwise the pipe buffer fills and we deadlock
                try {
                    proc_done = proc.waitFor(1L, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    break;
                }

                // read output from process "stdout"
                bytes = is.available();
                ba = new byte[bytes];
                is.read(ba);
                for (int i=0 ; i<bytes ; i++)
                    ib.append((char)ba[i]);

                // read output from process "stderr"
                bytes = es.available();
                ba = new byte[bytes];
                es.read(ba);
                for (int i=0 ; i<bytes ; i++)
                    eb.append((char)ba[i]);
            } while (! proc_done);

            // return results
            int proc_ret = proc.exitValue();
            if (statref != null)
                statref.assignTo(AST.IMASS, new Val(proc_ret, loc), loc);
            if (osref != null)
                osref.assignTo(AST.IMASS, new Val(ib.toString(), loc), loc);
            if (erref != null)
                erref.assignTo(AST.IMASS, new Val(eb.toString(), loc), loc);

        } catch (IOException e) {
            msg( e.getMessage());
            throw new ExEx("exec() - execution failed", loc);
        } catch (NullPointerException e) {
            throw new ExEx("exec() - command array is null or contains a null entry", loc);
        }
    }
}
