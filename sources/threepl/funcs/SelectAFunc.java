package threepl.funcs;

import static threepl.ThreePL.tdelist;

import threepl.ThreePL;
import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;
import threepl.codegen.TDEVar;
import threepl.exceptions.ExEx;
import threepl.exec.QueueRefs;
import threepl.exec.Ref;
import threepl.exec.Scope;
import threepl.exec.Type;
import threepl.exec.Val;
import threepl.exec.Var;
import threepl.exec.WordSpec;
import threepl.nodes.NodeList;
import threepl.parser.Constant;
import threepl.parser.SrcLoc;


/**
 * An inbuilt function to create a selector.
 * This has two arguments.
 *
 * <p>The 1st argument is a 1 dimensional array of type "log"
 * whose members select the corresponding data input. At most one
 * of these must be true at any time.
 *
 * <p>The 2nd argument is a 1 dimensional array of inputs, one of
 * which is to be selected.
 *
 * <p>The function has a defined value when at most one select input is
 * available and true and the associated data input is available, the value
 * reflecting the selected data input. If more than one input is selected the
 * value is undefined. If no inputs are selected, the value is zero or false
 * depending on data type(s).
 *
 * <p>If none of the function inputs contain queues, the function value is
 * always available. If any function inputs contain queues the function value
 * is available when an input selector and its associated data input are
 * available and the selector is true. Note that if some inputs contain
 * queues but one input selector and its associated data do not, the
 * function will be available when that selector is true. Note that the
 * use of this function as a gate with a clear value when no selectors are
 * true is only possible if the inputs contain no queues since any queues
 * in any inputs will render the function value unavailable when no
 * input is selected.
 */
public class SelectAFunc extends InbuiltFunc implements Constant, TDEConstants {

    /**
     * Calling interface for the function.
     * @param   args is a list of argument tree nodes
     * @return  the output
     */
    public Val getVal (NodeList args) {
        SrcLoc  loc = args.getCallLoc();
                
        if (args.size() != 2)
            throw new ExEx("selectA() must have 2 input arguments", loc);
        
        Scope       scope = ThreePL.getModuleScope();
        TDE         seltde = new TDE(TDEType.SELECT);
        QueueRefs    queues = new QueueRefs();
        // Select signal array.
        Val     sarray = args.getVal(0);
        if (sarray.getArrayType() == null)
            throw new ExEx("selectA() - select argument is not an array", loc);
        int[]   sdims = sarray.getDimDes();
        if ((sdims == null) || (sdims.length == 0))
            throw new ExEx("selectA() - select argument is not an array", loc);
        if (sdims.length != 1)
            throw new ExEx("selectA() - select argument is not a 1 dimensional array", loc);
        int     inputs = sdims[0];
        TDEVar[]    select_tdevars = new TDEVar[inputs];
        Val[]       select_vals = new Val[inputs];
        QueueRefs[]  select_queues = new QueueRefs[inputs];
        Val         sv;
        boolean     select_pointers = sarray.getMode() == Mode.IMMEDIATE;
        
        for (int i=0 ; i<inputs ; i++) {
            if (select_pointers) {
                Ref sr = (Ref)sarray.getVal(i);
                if (sr == null)
                    throw new ExEx("selectA() - select argument is null, array index " + i, loc);
                sv = sr.getVal(loc);
            } else {
                Var var = sarray.getVar();
                sv = (Val)var.getVal(i);
                if (sv == null)
                    throw new ExEx("selectA() - select argument is null, array index " + i, loc);
            }
            if (sv.getType().getPrimType() == Ptype.PTR)
                sv = sv.getSinglePval(loc).getVal(loc);
            if (!sv.isTarget())
                throw new ExEx("selectA() - select argument is not target type", loc);
            if (!(sv.getType().getPrimType() == Ptype.LOG))
                throw new ExEx("selectA() - select argument is not type log", loc);
            select_vals[i] = sv;
            select_tdevars[i] = sv.getTDEVar();
            select_queues[i] = sv.getQueues();
        }
        
        // Get queue refs from selector input argument.
        // If the argument has an examine operator this will be null
        // but further down we evaluate var[i] where 'var' is the argument
        // variable; this does not take account of a possible '?' on the
        // argument and hence we need 'have_squeues' to override any queue
        // references found later.
        boolean have_squeues = !sarray.getQueues().isEmpty();

        // Data array.
        Val     darray = args.getVal(1);
        if (darray.getArrayType() == null)
            throw new ExEx("selectA() - data argument is not an array", loc);
        int[]   ddims = darray.getDimDes();
        if ((ddims == null) || (ddims.length == 0))
            throw new ExEx("selectA() - data argument is not an array", loc);
        if (ddims.length != 1)
            throw new ExEx("selectA() - data argument is not a 1 dimensional array", loc);
        if (inputs != ddims[0])
            throw new ExEx("selectA() - data and select arrays different size", loc);
        TDEVar[]    data_tdevars = new TDEVar[inputs];
        Val[]       data_vals = new Val[inputs];
        QueueRefs[]  data_queues = new QueueRefs[inputs];
        Val         dv;
        boolean     data_pointers = darray.getMode() == Mode.IMMEDIATE;
        Type        dtype = null;
        Ptype       ptype = null;
        int         dwidth = 0;
        boolean     isPrimitive = false;
        for (int i=0 ; i<inputs ; i++) {
            if (data_pointers) {
                Ref dr = (Ref)darray.getVal(i);
                if (dr == null)
                    throw new ExEx("selectA() - data argument is null, array index " + i, loc);
                dv = dr.getVal(loc);
            } else {
                Var var = darray.getVar();
                dv = (Val)var.getVal(i);
                if (dv == null)
                    throw new ExEx("selectA() - data argument is null, array index " + i, loc);
            }
            if (!dv.isTarget())
                throw new ExEx("selectA() - data argument is not target type", loc);
            data_vals[i] = dv;
            data_tdevars[i] = dv.getTDEVar();
            data_queues[i] = dv.getQueues();
            if (i == 0) {
                // If first data input type is primitive, use this primitive type for the output
                // value and note the maximum width of the inputs.
                // If first data input is not primitive, save the type as the output type and
                // ensure that all inputs have exactly the same type.
                dtype = dv.getType();
                if (dtype.isPrimitive()) {
                    isPrimitive = true;
                    ptype = dtype.getPrimType();
                    dwidth = dtype.numBits();
                }
            } else {
                Type    t = dv.getType();
                if (isPrimitive) {
                    if (t.getPrimType() != ptype)
                        throw new ExEx("selectA() - data argument types not matched", loc);
                    if (t.numBits() > dwidth)
                        dwidth = t.numBits();
                } else {
                    if (!t.equals(dtype))
                        throw new ExEx("selectA() - data argument types not matched", loc);
                }
            }
        }
        
        TDEVar  otdev;
        Val     oval;
        Type    odtype;
        if (isPrimitive) {
            if (ptype == Ptype.LOG)
                odtype = new Type(Ptype.LOG, 1);
            else
                odtype = new Type(ptype.name().toLowerCase()+":"+dwidth, loc);
            WordSpec    odws = odtype.getWordSpec(null, loc);
            otdev = tdelist.signal("E", odws, loc);
            oval = new Val(null, Mode.VALUE, otdev, loc);
        } else {
            WordSpec    odws = dtype.getWordSpec(null, loc);
            otdev = tdelist.signal("E", odws, loc);
            oval = new Val(null, Mode.VALUE, otdev, loc);
        }
        
        for (int i=0 ; i<inputs ; i++) {
            // If select signal contains queue reads, AND the availability
            // signal(s) with the select signal so select can only
            // occur if queues are available.
            TDEVar          s = select_tdevars[i];
            TDEVar          d = data_tdevars[i];
            oval.addOVars(select_vals[i]);
            oval.addOVars(data_vals[i]);
            if (have_squeues && (select_queues.length == 0)) {
                Val     availval = select_queues[i].getBQAvail(scope, null, loc);
                TDEVar  avail = availval.getTDEVar();
                TDEVar  atdev = tdelist.and(s, avail, loc);
                seltde.add2i(atdev);
                select_queues[i].and(atdev, false, loc);
                select_queues[i].and_set(data_queues[i], loc);
                queues.or(select_queues[i], loc);
                oval.addOVars(availval);
            } else {
                seltde.add2i(s);
                data_queues[i].and(s, false, loc);
                queues.or(data_queues[i], loc);
            }
            seltde.add2i(d);
        }

        oval.andSetQueues(queues, loc);
        seltde.add2o(otdev);
        tdelist.addTDE(seltde);
        return(oval);
    }
}
