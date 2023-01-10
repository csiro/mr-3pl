/*
 * 3PL - Parallel Pipeline Programming Language
 *
 * Copyright (C) 2002-2003
 *     CSIRO Manufacturing and Infrastructure Technology
 */


package threepl.simulator;

import threepl.codegen.TDE;
import threepl.codegen.TDEConstants;


/**
 * The SDEOperator class implements the TDEType.OPERATOR TDE in the threepl
 * simulator.
 * 
 * @version $Revision: 6686 $
 * @author Andrew Tulloh
 */
public class SDEOperator extends SDE implements TDEConstants {

    // Temp storage used in RSH
    private SimValue in0Copy;
    
    /** The operator type (see TDEConstants.java) */
    TDEOp operatorType;

    /**
     * Constructor
     * @param sim The single Sim object
     * @param tde Associated TDE
     */
    protected SDEOperator(Sim sim, TDE tde) {
        super(sim, tde);
    }

    /**
     * Do combinatorial behaviour
     * @param oIndex Index of the output to calculate
     * @param time The current simulator time
     * @param force True if behaviour should happen regardless of time
     * @return True (indicates behaviour is done for this output)
     */
    public boolean behaviour(int oIndex, double time, boolean force) {
    
       // If already done for this time, no action..
       if (super.behaviour(oIndex, time, force))
          return true;

       // ..else do output behaviour
       oCalc[oIndex] = true;
       	  
       // Get shortcuts to input operand(s) and output result(s)
       SimValue in0 = getInputValue(0, time, force);
       SimValue in1 = null;

       if(inputs.length > 1)
           in1 = getInputValue(1, time, force);

       SimValue out = getOutput(0).getValue();

       // Act according to operator
       switch(operatorType) {

       // output = ~input
       case INV:
           out.put(in0).inv();
           break;

       // output = -input
       case NEG:
           out.put(in0).neg();
           break;

       // output = input[0] + input[1]
       case ADD:
           out.put(in0).add(in1);
           break;

       // output = input[0] - input[1]
       case SUB:
           out.put(in0).sub(in1);
           break;

       // if (input[3]==true)
       //    if (input[2]==true)
       //       output[0] = input[0] + input[1]
       //    else
       //       output[0] = input[0] - input[1]
       // else
       //   output[0] = input[0]
       //
       case ADDSUB:
          if (getInputValue(3, time, force).getBit()) {
	     if (getInputValue(2, time, force).getBit())
	        out.put(in0).add(in1);
             else
	        out.put(in0).sub(in1);		
	  } else
	     out.put(in0);
          break;
	  
       // output = input[0] * input[1]
       case MUL:
           out.put(in0).mul(in1);
           break;

       // output = input[0] / input[1]
       case DIV:
           out.put(in0).div(in1);
           break;

       // output = input[0] % input[1]
       case REM:
           out.put(in0).rem(in1);
           break;

       // output[0] = input[0] / input[1]
       // output[1] = input[0] % input[1]
       case DIVREM:
          switch (oIndex) {
	  case 0:
	     out.put(in0);
	     break;
	  case 1:
             SimValue out1 = outputs[1].getValue();
	     out1.put(out.put(in0).divrem(in1));
             break;
          }	   
          break;

       // output = input[0] == input[1]
       case EQ:
           out.put(in0.eq(in1));
           break;

       // output = input[0] != input[1]
       case NE:
           out.put(in0.ne(in1));
           break;

       // output = input[0] < input[1]
       case LT:
           out.put(in0.lt(in1));
           break;

       // output = input[0] <= input[1]
       case LE:
           out.put(in0.le(in1));
           break;

       // output = input[0] > input[1]
       case GT:
           out.put(in0.gt(in1));
           break;

       // output = input[0] >= input[1]
       case GE:
           out.put(in0.ge(in1));
           break;

       // output = input[0] & input[1] & ... & input[n-1]
       case AND:
           out.put(in0).bitwiseAnd(in1);
           break;
       /*case NAND:
           out.put(in0).bitwiseAnd(in1).bitwiseNot();
           break;*/
       /*case _AND:
           out.put(in0.bitwiseNot()).bitwiseAnd(in1);
           break;*/
       /*case AND_:
           out.put(in0).bitwiseAnd(in1.bitwiseNot());
           break;*/

       // output = input[0] | input[1] | ... | input[n-1]
       case OR:
           out.put(in0).bitwiseOr(in1);
           break;
       /*case NOR:
           out.put(in0).bitwiseOr(in1).bitwiseNot();
           break;*/
       /*case _OR:
           out.put(in0.bitwiseNot()).bitwiseOr(in1);
           break;*/
       /*case OR_:
           out.put(in0).bitwiseOr(in1.bitwiseNot());
           break;*/

       // output = input[0] ^ input[1] ^ ... ^ input[n-1]
       case XOR:
           out.put(in0).bitwiseXor(in1);
           break;
       /*case XNOR:
           out.put(in0).bitwiseXor(in1).bitwiseNot();
           break;*/

       // output = input[0] << input[1]
       case LSH:
           out.put(in0).lsh(in1);
           break;

       // output = input[0] >> input[1]
       case RSH:
           out.put(in0Copy.put(in0).rsh(in1));
           break;

       // output = input[0] ? input[1] : input[2]
       case COND:
           out.put(in0.getBit() ? in1 : getInputValue(2, time, force));
           break;
	  
       default:
           throw new SimException(
	      "SDEOperator.behavior():invalid operator type ("+operatorType+")");
       }

       oCalc[oIndex] = false;
       return true;     	  
    }


    /** Build variables */
    public void buildVariables() {
        super.buildVariables();

        // Check there is a parameter
	if (params.size() == 0)
	   throw new SimException(
	      "SDEOperator.buildVariables():no parameter supplied");

        // First param is operator type, remaining params relate
	// to operand types and are ignored.
        operatorType = (TDEOp) getParam(0);
	
	// Need a copy of the first input for RSH
	if (operatorType==TDEOp.RSH)
	   in0Copy = ((SimVariable)inputs[0]).getValue().duplicate();
    }
}
