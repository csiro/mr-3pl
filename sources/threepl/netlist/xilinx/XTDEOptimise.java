package threepl.netlist.xilinx;

import static threepl.ThreePL.family;
import static threepl.ThreePL.tdelist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import threepl.codegen.TDE;
import threepl.codegen.TDEVar;
import threepl.codegen.TDEConstants.TDEOp;
import threepl.codegen.TDEConstants.TDEType;
import threepl.codegen.TDEConstants.TDEVtype;
import threepl.exceptions.ExEx;
import threepl.exec.WordSpec;
import threepl.parser.SrcLoc;
import threepl.parser.Constant.Ptype;

/**
 * This class contains static methods to optimise family-specific ALUs
 * in the TDE list.
 */
public class XTDEOptimise {
    
    /**
     * TDE list optimisation specific to the FPGA family.
     */
    public static void optimiseTDEs () {
        // Find any SELECT TDEs which have an ALU flag and optimise
        // by moving any input ALU operations into a shared single ALU
        // between the selector and its static, queue or selectvalue variable.
        //
        // THIS OPTIMISATION IS MESSY AND MAY NOT BE WORTH EXPLOITING.
        // PERHAPS IT SHOULD NOT BE INCLUDED>
        optimiseALUs();
        
        // Find any SELECT TDEs which have an ALU flag, have a single input
        // and have not been optimised out and remove them, connecting the
        // single input to the output.
        optimiseSelectors();
        
        // Find any REG TDEs which have a DSP flag and change them
        // to DSP48 elements which just use the P register.
        // If the dsp reg width is 0 there are no DSP elements, so skip this.
        //
        // Note that the performance of these registers will be inferior
        // to those using CLB flip-flops as the clock-to-output time is much longer!
        // For fairly low clock rates however this can save CLB resources if many DSP48s/MULTs
        // are spare.
        if (((XTDECode)family).DSPRegWidth() != 0)
            convertDSPRegisters();

        // Find any multipliers and use hardware multipliers where available and suitable.
        // If the dsp reg width is 0 there are no suitable hardware elements, so skip this.
        //if (((XTDECode)family).DSPRegWidth() != 0)
        //    multipliersToHardware();             
    }
    
    /** 
     * Find any multipliers and use hardware multipliers where available and suitable.
     * NOW REDUNDANT.
     */
    private static void multipliersToHardware () {
        for (TDE tde : tdelist.getOps()) {
            if (!tde.isActive())
                continue;   // not an active TDE - skip
            if ((TDEOp)tde.getParam(0) != TDEOp.MUL)
                continue;   // not a multiplier TDE - skip
            if (family.MultWidth1() == 0)
                continue;   // no multiplier hardware - skip (use CLB logic)
            
            TDEVar  a = tde.getInput(0);
            TDEVar  b = tde.getInput(1);
            TDEVar  p = tde.getOutput(0);
            
            /*
            //
            // Swap operands so a is the larger.
            // NO NEED AS ALREADY DONE IN nodes/ArgParams().
            //
            int     na = a.numBits();
            int     nb = b.numBits();
            boolean asigned = (Boolean)tde.getParam(1);
            boolean bsigned = (Boolean)tde.getParam(2);
            int     i;
            TDEVar  t;
            if (nb > na) {
                i = na; na = nb; nb = i;
                t = a; a = b; b = t;
            }
            
            // Currently ArgParams() exits with width error if these widths are exceeded so this
            // code is not executed. In any case would not really want to use the CLBlogic multiplier!
            //
            if (asigned && (na > family.MultWidth1()) || !asigned && (na >= family.MultWidth1()))
                continue;   // use CLB logic if operand a is too big for hardware
            if (bsigned && (nb > family.MultWidth2()) || !bsigned && (nb >= family.MultWidth2()))
                continue;   // use CLB logic if operand b is too big for hardware
            */
            
            tde.deactivate();                   // delete the old TDE
            ((XTDECode)family).Mult(a, b, p);   // replace with a hardware multiplier
        }
    }
    
    /** Find any SELECT TDEs which have an ALU flag, have a single input
     * and have not been optimised out and remove them, connecting the
     * single input to the output.
     */
    private static void optimiseSelectors () {
        for (TDE tde : tdelist.getSelects()) {
            if (!tde.isActive())
                continue;
            boolean aluflag = false;
            if (tde.getParams().size() > 1)
                aluflag = ((Boolean)tde.getParam(1)).booleanValue();
            if (!aluflag)
                continue;   // no ALU flag
            if (tde.getInputs().size() > 2)
                continue;   // more than one select:data input pair
            
            TDEVar  din = tde.getInput(1);
            TDEVar  out = tde.getOutput(0);
            tde.deactivate();
            tdelist.connect(out, din);
        }
    }
    
    /**
     * Find any REG TDEs which have a DSP flag and change them
     * to DSP48 elements which just use the P register.
     */
    private static void convertDSPRegisters () {
        for (TDE tde : tdelist.getRegs()) {
            if ((Boolean)tde.getParam(2))  // Check for IOB flag - if set cannot use a DSP block, so skip this register.
                continue;
            if (!(Boolean)tde.getParam(3)) // Check for DSP flag - if not set, skip this register.
                continue;
            
            boolean done = false;
            
            // See if the data input to the register comes from a hardware multiplier
            // or DSP block. Do this by tracking back through any CONNECT TDEs to a
            // source.
            TDEVar  data = tde.getInput(1);
            
            // Find an ELEMENT TDE source for TDEVar 'data' if it exists,
            // returning null otherwise.
            TDE     stde = findSourceTDE(data);
         
            // Check that the ELEMENT TDE data source is a MULT18X18 or DSP48...
            if (stde != null) {
                String  ename = (String)stde.getParam(0);
                if (ename.startsWith("MULT18X18") || ename.startsWith("DSP48")) {
                    // Make sure the data source does not go anywhere else. It may go to
                    // other TDE CONNECTs but that is OK if their outputs do not go anywhere.
                    if (stde.getOutputs().size() == 1) { // make sure only output is used
                        TDEVar dsrc = stde.getOutput(0);
                        if (singleDest(dsrc, tde)) {
                            // Check that the TDE output is not already registered (is MULT18X18S or property "PREG" == 1).
                            boolean mreg = ename.equals("MULT18X18S");      // is a registered-output multiplier
                            String  preg = stde.getELementProperty("PREG"); // is a DSP48 with property "PREG" == 1
                            if (!mreg && ((preg == null) || !preg.equals("1"))) {
                                // Subsume the register into the MULT or DSP block.
                                TDEVar  clk = tde.getInput(0);
                                TDEVar  ce = tde.getInput(2);
                                TDEVar  rst = tde.getInput(3);
                                TDEVar  o = tde.getOutput(0);
                                tde.deactivate();
                                ((XTDECode)family).regIntoDSP(stde, clk, ce, rst, o);
                                done = true;
                            }
                        }
                    }
                }              
            }
            
            // Otherwise just change the REG TDE into a MULT18X18S or DSP48.. TDE ELEMENT.
            if (!done)
                ((XTDECode)family).regToDSP(tde);
        }
    }

    /**
     * Search backwards from a TDEVar to find a source that is a TDE of type ELEMENT.
     * If a constant TDEVar is found or a TDE that is not type ELEMENT then null
     * is returned. The search will pass back through CONNECT TDEs.
     * @param data is the starting TDEVar
     * @return the ELEMENT TDE or else null
     */   
    private static TDE findSourceTDE(TDEVar data) {
        TDE     stde = data.getSingleSrc();
        if (stde == null)
            return(null);
        while (stde.getType() == TDEType.CONNECT) {
            data = stde.getInput(0);
            if (data == null)
                return(null);
            stde = data.getSingleSrc();
            if (stde == null)
                return(null);
        }        
        // Check that the data source is a TDE type ELEMENT.
        if (stde.getType() == TDEType.ELEMENT)
            return(stde);
        else
            return(null);
    }
    
    /**
     * Follow all signal paths forward from 'd' through any CONNECT TDEs until
     * a CONNECT with no output signal is encountered or TDE 'tde' is encountered.
     * If any other TDE is encountered return false.
     * @param d is the starting signal
     * @param tde is a terminating TDE
     * @return true if single destination is found
     */
    private static boolean singleDest (TDEVar d, TDE tde) {
        for (TDE t : d.getDestList()) {
            if (t == tde)
                continue;
            if (t.getType() != TDEType.CONNECT)
                return(false);
            TDEVar  o = t.getOutput(0);
            if (o == null)
                continue;
            if (!singleDest(o, tde))
                return(false);
        }
        return(true);
    }

    /**
     * Optimise arithmetic and logic operations into selectors
     * which have been flagged as allowing a substituted ALU.
     */
    private static void optimiseALUs () {
        ArrayList<TDE>  new_tdes = new ArrayList<TDE>();
        ArrayList<TDE>  l = tdelist.getSelects();
        for (TDE tde : l) {
            if (!tde.isActive())
                continue;
            boolean aluflag = false;
            boolean dspflag = false;
            if (tde.getParams().size() > 1)
                aluflag = ((Boolean)tde.getParam(1)).booleanValue();
            if (tde.getParams().size() > 2)
                dspflag = ((Boolean)tde.getParam(2)).booleanValue();
            if (!aluflag)
                continue;   // does not have the ALU flag

            // Flags ignored if not one of the following -
            if ((family instanceof XC5V) || (family instanceof XC6V) || (family instanceof XC7)) {
                if (dspflag)
                    optimise_dsp48_alu(tde, new_tdes); // generate DSP48 ALU
                else
                    optimise_clb_alu(tde, new_tdes);   // generate CLB ALU
            } else if (!((XTDECode)family).needCPLDarith()) {
                optimise_clb_alu(tde, new_tdes);
            }
        }
        for (TDE t : new_tdes)
            tdelist.addTDE(t);
    }
    
    /**
     * If possible substitute a DSP48 ALU for a selector with
     * inputs which are arithmetic or logical operations.
     * 
     * THIS IS FOR A DSP48E and DSP48E1 - NEED CHANGES FOR DSP48, DSP48A1
     * 
     * @param tde is a selector TDE with an ALU or DSP flag
     * @param new_tdes is a list of newly created TDEs to be added later to the TDE list
     */
    private static void optimise_dsp48_alu(TDE tde, ArrayList<TDE> new_tdes) {
        // Generate CLB ALU.
        int             n = tde.getInputs().size() / 2;
        int             i;
        int             alu_ops = 0;
        boolean         mult_used = false;
        ArrayList<TDE>  trash = new ArrayList<TDE>();
        TDEVar[]        sel = new TDEVar[n];
        TDEVar[]        in1 = new TDEVar[n];
        TDEVar[]        in2 = new TDEVar[n];
        boolean[]       sw = new boolean[n];
        boolean[]       mul = new boolean[n];
        long[]          opmodes = new long[n];
        long[]          alumodes = new long[n];
        TDEOp[]         ops = new TDEOp[n];

        for (i=0 ; i<n ; i++) {
            TDEVar  sin = tde.getInput(2*i);
            TDEVar  din = tde.getInput(2*i+1);
            TDE     dtde = din.getSingleSrc();
            
            // See if there is an intervening CONNECT between the data source and the ALU input.
            // If so, connect the ALU input to the actual data source and get rid of the CONNECT TDE.
            // The following checks for destination lists of size 1 may be invalid
            // in some cases, though have not found any such.
            if (dtde.getType() == TDEType.CONNECT) {
                // There is an intervening CONNECT between the data input and its source
                if (din.getDestListSize() == 1) {       // make sure the CONNECT output only goes to one destination
                    TDEVar  din_ = dtde.getInput(0);    // get the input to the CONNECT
                    if (din_.getDestListSize() == 1) {  // make sure that data source only goes to one destination
                        dtde.deactivate();                  // remove intervening CONNECT TDE
                        din = din_;                     // step back down the input chain to the data source
                        dtde = din.getSingleSrc();      // get the associated TDE and use this as the actual ALU input
                    }
                }
            }
            
            if ((dtde != null) && (din.getType() == TDEVtype.VAR)) {
                SrcLoc  loc = dtde.getSrcLoc();
                switch (dtde.getType()) {
                case OPERATOR:
                    TDEOp op = (TDEOp)dtde.getParam(0);
                    ops[i] = op;
                    switch (op) {
                    case ADD:
                    case AND:
                    //case NAND:
                    //case _AND:
                    //case AND_:
                    case OR:
                    //case NOR:
                    //case _OR:
                    //case OR_:
                    case XOR:
                    //case XNOR:
                        sel[i] = sin;
                        in1[i] = dtde.getInput(0);
                        in2[i] = dtde.getInput(1);
                        sw[i] = true;
                        mul[i] = false;
                        switch (op) {
                        case ADD:  opmodes[i] = 0x33; alumodes[i] = 0x0; break;
                        case AND:  opmodes[i] = 0x33; alumodes[i] = 0xc; break;
                        //case NAND: opmodes[i] = 0x33; alumodes[i] = 0xe; break;
                        //case _AND: opmodes[i] = 0x33; alumodes[i] = 0xd; break;
                        //case AND_: opmodes[i] = 0x3b; alumodes[i] = 0xf; break;
                        case OR:   opmodes[i] = 0x3b; alumodes[i] = 0xc; break;
                        //case NOR:  opmodes[i] = 0x3b; alumodes[i] = 0xe; break;
                        //case _OR:  opmodes[i] = 0x3b; alumodes[i] = 0xd; break;
                        //case OR_:  opmodes[i] = 0x33; alumodes[i] = 0xf; break;
                        case XOR:  opmodes[i] = 0x3b; alumodes[i] = 0x5; break;
                        //case XNOR: opmodes[i] = 0x33; alumodes[i] = 0x6;
                        default:
                            break; 
                        }
                        alu_ops++;
                        trash.add(dtde);
                        break;
                    case SUB:
                        sel[i] = sin;
                        in1[i] = dtde.getInput(0);
                        in2[i] = dtde.getInput(1);
                        sw[i] = false;
                        mul[i] = false;
                        opmodes[i] = 0x33;
                        alumodes[i] = 3;
                        alu_ops++;
                        trash.add(dtde);
                        break;
                    case MUL:
                        sel[i] = sin;
                        int n1 = dtde.getInput(0).numBits();
                        int n2 = dtde.getInput(1).numBits();
                        if (n2 > 18) {
                            if (n2 > 25)
                                throw new ExEx("multiplier 2nd operand > 25 bits", loc);
                            if (n1 > 18)
                                throw new ExEx("multiplier both operands > 18 bits", loc);
                            in1[i] = dtde.getInput(1);
                            in2[i] = dtde.getInput(0);
                        } else {
                            if (n1 > 25)
                                throw new ExEx("multiplier 1st operand > 25 bits", loc);
                            in1[i] = dtde.getInput(0);
                            in2[i] = dtde.getInput(1);
                        }
                        sw[i] = true;
                        mul[i] = (n1 <= 18) && (n2 <= 18);
                        opmodes[i] = 0x5;
                        alumodes[i] = 0;
                        alu_ops++;
                        mult_used = true;
                        trash.add(dtde);
                        break;
                    default:
                        sel[i] = sin;
                        in1[i] = null;
                        in2[i] = din;
                        sw[i] = false;
                        mul[i] = false;
                        opmodes[i] = 0x30;
                        alumodes[i] = 0x00;
                        ops[i] = TDEOp.ASSIGN;
                    }
                    break;
                default:
                    sel[i] = sin;
                    in1[i] = null;
                    in2[i] = din;
                    sw[i] = false;
                    mul[i] = false;
                    opmodes[i] = 0x30;
                    alumodes[i] = 0x00;
                    ops[i] = TDEOp.ASSIGN;
                }
            } else {
                sel[i] = sin;
                in1[i] = null;
                in2[i] = din;
                sw[i] = false;
                mul[i] = false;
                opmodes[i] = 0x30;
                alumodes[i] = 0x00;
                ops[i] = TDEOp.ASSIGN;
            }
        }
        swap(in1, in2, sw);
        swap(in1, in2, mul);
        if (alu_ops > 0) {
            // remove adders, subtracters and multipliers
            for (TDE t : trash)
                t.deactivate();

            // replace multiple adders/subtracters/multipliers/logic-ops by
            // single DSP48
            TDEVar      out = tde.getOutput(0);
            WordSpec    wsA = new WordSpec(30, Ptype.BITS);
            WordSpec    wsB = new WordSpec(18, Ptype.BITS);
            WordSpec    wsC = new WordSpec(48, Ptype.BITS);
            WordSpec    wsopmode = new WordSpec(7, Ptype.BITS);
            WordSpec    wsalumode = new WordSpec(4, Ptype.BITS);
            TDE         alu_selA = new TDE(TDEType.SELECT);
            TDE         alu_selB = new TDE(TDEType.SELECT);
            TDE         alu_selC = new TDE(TDEType.SELECT);
            TDE         opmode_sel = new TDE(TDEType.SELECT);
            TDE         alumode_sel = new TDE(TDEType.SELECT);
            TDEVar      alu_inA = tdelist.signal("ALUINA", wsA, null);
            TDEVar      alu_inB = tdelist.signal("ALUINB", wsB, null);
            TDEVar      alu_inC = tdelist.signal("ALUINC", wsC, null);
            TDEVar      opmode = tdelist.signal("DSPOPMODE", wsopmode, null);
            TDEVar      alumode = tdelist.signal("DSPALUMODE", wsalumode, null);
            
            tde.deactivate();    // remove the selector.
            ((XTDECode)family).dsp48_alu(mult_used, out, alu_inA, alu_inB, alu_inC, opmode, alumode);

            alu_selA.add2o(alu_inA);
            alu_selB.add2o(alu_inB);
            alu_selC.add2o(alu_inC);
            opmode_sel.add2o(opmode);
            alumode_sel.add2o(alumode);
            opmode_sel.setNoDelete();
            alumode_sel.setNoDelete();

            for (i=0; i<n ; i++) {
                switch (ops[i]) {
                case ADD:
                case SUB:
                case AND:
                //case NAND:
                //case _AND:
                //case AND_:
                case OR:
                //case NOR:
                //case _OR:
                //case OR_:
                case XOR:
                //case XNOR:
                    alu_selA.add2i(sel[i]);
                    alu_selA.add2i(upper_30(in2[i]));
                    alu_selB.add2i(sel[i]);
                    alu_selB.add2i(lower_18(in2[i]));
                    alu_selC.add2i(sel[i]);
                    alu_selC.add2i(in1[i]);
                    break;
                case MUL:
                    alu_selA.add2i(sel[i]);
                    alu_selA.add2i(in1[i]);
                    alu_selB.add2i(sel[i]);
                    alu_selB.add2i(in2[i]);
                    break;
                case ASSIGN:
                    if (in1[i] != null) {
                        alu_selC.add2i(sel[i]);
                        alu_selC.add2i(in1[i]);
                    } else {
                        alu_selC.add2i(sel[i]);
                        alu_selC.add2i(in2[i]);
                    }
                    break;
                default:
                    break;
                }
                opmode_sel.add2i(sel[i]);
                opmode_sel.add2i(new TDEVar(opmodes[i], Ptype.BITS, null));
                alumode_sel.add2i(sel[i]);
                alumode_sel.add2i(new TDEVar(alumodes[i], Ptype.BITS, null));
            }

            new_tdes.add(alu_selA);
            new_tdes.add(alu_selB);
            new_tdes.add(alu_selC);
            new_tdes.add(opmode_sel);
            new_tdes.add(alumode_sel);
        }
    }
    
    private static TDEVar upper_30 (TDEVar in) {
        WordSpec    ws = in.getWordSpec();
        int n = in.numBits();
        if (n <= 18)
            return(TDEVar.GND);
        else {
            Ptype       ptype = ws.getPrimType(0);
            WordSpec    ows = new WordSpec(30, ptype);
            TDEVar      tdev = tdelist.signal("UPPER30", ows, null);
            tdelist.lshift(tdev, in, -18, null);
            return(tdev);
        }
    }
    
    private static TDEVar lower_18 (TDEVar in) {
        WordSpec    ws = in.getWordSpec();
        int n = in.numBits();
        if (n <= 18)
            return(in);
        else {
            Ptype       ptype = ws.getPrimType(0);
            WordSpec    ows = new WordSpec(18, ptype);
            TDEVar      tdev = tdelist.signal("LOWER18", ows, null);
            tdelist.connect(tdev, in, false);
            return(tdev);
        }
    }
    
    // If possible substitute a CLB ALU for a selector with
    // inputs which are arithmetic operations.
    private static void optimise_clb_alu (TDE tde, ArrayList<TDE> new_tdes) {
        // Generate CLB ALU.
        int                 n = tde.getInputs().size() / 2;
        int                 i;
        int                 alu_ops = 0;
        ArrayList<TDE>      trash = new ArrayList<TDE>();
        TDEVar[]            sel = new TDEVar[n];
        TDEVar[]            in1 = new TDEVar[n];
        TDEVar[]            in2 = new TDEVar[n];
        boolean[]           sw = new boolean[n];
        HashSet<TDEVar>     adds = new HashSet<TDEVar>();
        HashSet<TDEVar>     addsubs = new HashSet<TDEVar>();

        // add and subtract operations are inserted at the
        // heads of the lists
        // assignments are appended to the tails of the lists
        for (i=0 ; i<n ; i++) {
            TDEVar  sin = tde.getInput(2*i);
            TDEVar  din = tde.getInput(2*i+1);
            TDE     dtde = din.getSingleSrc();
            if ((dtde != null) && (din.getType() == TDEVtype.VAR)) {
                switch (dtde.getType()) {
                case OPERATOR:
                    switch ((TDEOp)dtde.getParam(0)) {
                    case ADD:
                        sel[i] = sin;
                        in1[i] = dtde.getInput(0);
                        in2[i] = dtde.getInput(1);
                        sw[i] = true;
                        adds.add(sin);
                        addsubs.add(sin);
                        alu_ops++;
                        trash.add(dtde);
                        break;
                    case SUB:
                        sel[i] = sin;
                        in1[i] = dtde.getInput(0);
                        in2[i] = dtde.getInput(1);
                        sw[i] = false;
                        addsubs.add(sin);
                        alu_ops++;
                        trash.add(dtde);
                        break;
                    default:
                        sel[i] = sin;
                        in1[i] = null;
                        in2[i] = din;
                        sw[i] = false;
                    }
                    break;
                default:
                    sel[i] = sin;
                    in1[i] = null;
                    in2[i] = din;
                    sw[i] = false;
                }
            } else {
                sel[i] = sin;
                in1[i] = null;
                in2[i] = din;
                sw[i] = false;
            }
        }
        
        // If there are no multiple ALU operations to combine, return.
        if (alu_ops <= 1)
            return;
        
        // Combine multiple ALU operations.
        
        swap(in1, in2, sw);
        // remove adders and subtracters
        for (TDE t : trash)
            t.deactivate();

        // replace multiple adders/subtracters by single ALU
        TDEVar  out = tde.getOutput(0);
        tde.deactivate();    // remove the selector.
        int         width = out.numBits();
        WordSpec    ws = new WordSpec(width, Ptype.BITS);
        TDE         alu = new TDE(TDEType.OPERATOR);
        TDE         alu_sel1 = new TDE(TDEType.SELECT);
        TDE         alu_sel2 = new TDE(TDEType.SELECT);
        TDEVar      alu_in1 = tdelist.signal("ALUIN1", ws, null);
        TDEVar      alu_in2 = tdelist.signal("ALUIN2", ws, null);
        alu.add2p(TDEOp.ALU);
        alu.add2p(false); // 1st operand signed? treat as unsigned
        alu.add2p(false); // 2nd operand signed? treat as unsigned
        alu.add2p(false); // result signed? treat as unsigned
        alu.add2p(false); // DSP? not for these FPGAs
        alu.add2i(alu_in1);
        alu.add2i(alu_in2);
        alu_sel1.add2o(alu_in1);
        alu_sel2.add2o(alu_in2);
        if (adds.size() == addsubs.size()) {
            TDEVar  tdev = tdelist.or(adds, null);
            alu.add2i(tdev);
            alu.add2i(tdev);
        } else {
            alu.add2i(tdelist.or(adds, null));
            alu.add2i(tdelist.or(addsubs, null));
        }
        alu.add2o(out);
        for (i=0 ; i<n ; i++) {
            if (in1[i] != null) {
                alu_sel1.add2i(sel[i]);
                alu_sel1.add2i(in1[i]);
            }
            if (in2[i] != null) {
                alu_sel2.add2i(sel[i]);
                alu_sel2.add2i(in2[i]);
            }
        }
        new_tdes.add(alu_sel1);
        new_tdes.add(alu_sel2);
            new_tdes.add(alu);
    }
    
    // Swap operands for commutative operators to avoid duplication
    // of the same operand on both input selectors where possible.
    // This is probably not optimal but should work in most cases.
    private static void swap(
        TDEVar[]   in1,
        TDEVar[]   in2,
        boolean[]  sw
    ) {
        // Map TDEVars from both lists to unique integer labels.
        // Construct matching arrays of these labels along with
        // swapable array.
        int             n = sw.length;
        HashSet<TDEVar> s = new HashSet<TDEVar>();
        HashSet<TDEVar> s1sw = new HashSet<TDEVar>();
        HashSet<TDEVar> s1nsw = new HashSet<TDEVar>();
        HashSet<TDEVar> s2sw = new HashSet<TDEVar>();
        HashSet<TDEVar> s2nsw = new HashSet<TDEVar>();
        TDEVar  t;
        for (int i=0 ; i<n ; i++) {
            if (sw[i]) {
                s1sw.add(t=in2[i]);
                s.add(t);
                s2sw.add(t=in1[i]);
                s.add(t);
            } else {
                s1nsw.add(t=in1[i]);
                s.add(t);
                s2nsw.add(t=in2[i]);
                s.add(t);
            }
        }
        Iterator<TDEVar>    it = s.iterator();
        int                 col = 0;
        while (it.hasNext()) {
            t = it.next();
            it.remove();
            if (t == null)
                continue;
            if (s1nsw.contains(t) && s2nsw.contains(t))
                continue;
            if (s1nsw.contains(t) && s2sw.contains(t)) {
                swapt(in1, in2, n, t);
                continue;
            }
            if (s2nsw.contains(t) && s1sw.contains(t)) {
                swapt(in2, in1, n, t);
                continue;
            }
            if (s1sw.contains(t) && s2sw.contains(t)) {
                if (col == 0) {
                    swapt(in1, in2, n, t);
                    col = 1;
                } else {
                    swapt(in2, in1, n, t);
                    col = 0;
                }
            }
        }
    }
    
    private static void swapt (
        TDEVar[]    in1,
        TDEVar[]    in2,
        int         n,
        TDEVar      tdev
    ) {
        TDEVar  t;
        for (int i=0 ; i<n ; i++) {
            if (tdev.equals(in2[i])) {
                t = in1[i];
                in2[i] = t;
                in1[i] = tdev;
            }
        }
    }
}
