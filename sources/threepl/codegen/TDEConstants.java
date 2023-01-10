package threepl.codegen;

import threepl.parser.Constant;

public interface TDEConstants extends Constant {
    // codes representing expression operators
    // The CCGL code integers are only used for passing to
    // CCGL code in the TDECode.OPERATOR.
    public enum TDEOp {
        INV      ("~"),
        NEG      ("-"),
        ADD      ("+"),
        SUB      ("-"),
        ADDSUB   ("+-"),
        ALU      ("+-<-"),
        MUL      ("*"),
        DIV      ("/"),
        REM      ("%"),
        DIVREM   ("/%"),
        EQ       ("=="),
        NE       ("!="),
        LT       ("<"),
        LE       ("<="),
        GT       (">"),
        GE       (">="),
        AND      ("&"),
        //NAND     ("~(&)"),
        //_AND     ("~ &"),
        //AND_     ("& ~"),
        OR       ("|"),
        //NOR      ("~(|)"),
        //_OR      ("~ |"),
        //OR_      ("| ~"),
        XOR      ("^"),
        //XNOR     ("~(^)"),
        LSH      ("<<"),
        RSH      (">>"),
        LROT     ("<r<"),
        RROT     (">r>"),
        COND     ("?:"),
        ASSIGN   ("<-"); // special case - used for assignment
        private final String    name;        
        TDEOp (String n) {
            name = n;
        }        
        public String opstring () {return(name);}
    }

    // TDEVar types
    public enum TDEVtype {
        VAR     (Ptype.NONE),   // variable
        POS_CLK (Ptype.LOG),    // positive-edge clock
        NEG_CLK (Ptype.LOG),    // negative-edge clock
        BITS    (Ptype.BITS),   // bits
        UINT    (Ptype.UINT),   // unsigned integer constant
        INT     (Ptype.INT),    // integer constant
        UFIXED  (Ptype.UFIXED), // unsigned fixed point constant
        FIXED   (Ptype.FIXED),  // fixed point constant
        FLOAT   (Ptype.FLOAT),  // floating point constant
        BOOL    (Ptype.LOG);    // boolean constant
        private Ptype   ptype;
        TDEVtype (Ptype pt) {
            ptype = pt;
        }
        public Ptype primtype () {return(ptype);}
    }

    // TDEVar port types
    public enum TDEVPtype {
        NONE,   // not a port
        INPUT,  // input port
        OUTPUT, // output port
        INOUT;  // bidirectional port
    }

    // TDE types
    public enum TDEType {
        SIG             ("SIG"            ), // declare signal
        CONNECT         ("CONNECT"        ), // connect signals
        SELECT          ("SELECT"         ), // selector
        TSELECT         ("TSELECT"        ), // 3-state selector
        REG             ("REG"            ), // static variable
        QUEUEBUFFER     ("QUEUEBUFFER"    ), // queue buffer
        WAIT            ("WAIT"           ), // parallel block complete
        DIVERGE         ("DIVERGE"        ), // queue diverge
        OPERATOR        ("OPERATOR"       ), // an operator
        EXECP           ("EXECP"          ), // delayed exec on queue avail
        ILOOP           ("ILOOP"          ), // infinite loop
        START           ("START"          ), // start pulse from level
        DEL             ("DEL"            ), // delay
        WHEN            ("WHEN"           ), // when
        WHILE           ("WHILE"          ), // while
        DOWHILE         ("DOWHILE"        ), // do while
        PRIORITY        ("PRIORITY"       ), // priority encoder
        RESYNC          ("RESYNC"         ), // pulse/level resynchroniser
        SAMPLE          ("SAMPLE"         ), // sample in different clock domain
        IBUF            ("IBUF"           ), // input buffer
        OBUF            ("OBUF"           ), // output buffer
        INV             ("INV"            ), // 1's complement
        AND             ("AND"            ), // multiple input AND gate
        OR              ("OR"             ), // multiple input OR gate
        XOR             ("XOR"            ), // multiple input XOR gate
        DFF             ("DFF"            ), // D flip flop
        CRAM            ("CRAM"           ), // combinatorial RAM
        RRAM            ("RRAM"           ), // registered output RAM
        ELEMENT         ("ELEMENT"        ), // new EDIF element
        PORT            ("PORT"           ), // connect signal to external port
        SPECIAL         ("SPECIAL"        ), // vendor-specific elements
        SIMREAD         ("SIMREAD"        ), // simulation line read from file
        SIMWRITE        ("SIMWRITE"       ), // simulation line written to file
        SIMVAR          ("SIMVAR"         ); // simulator variable notification
        private final String    name;        
        TDEType (String n) {
            name = n;
        }        
        public String typename () {return(name);}
    }
}
