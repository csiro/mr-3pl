package threepl.parser;

import threepl.exceptions.ExEx;
import threepl.exec.WordSpec;

/**
 * A collection of constants.
 * This interface is used to define a set of global constants used 
 * throughout the compiler.
 */
public interface Constant {

    // separator for hierarchical names, e.g.
    // main.function2_1
    static final String hsep = ".";
    // separator for call count appended to a hierarchical name component
    // e.g. function1_4
    static final String ccsep = "_";
    // separator for appendices such as "NE", "POP", "D" and "CE"
    static final String ctrail = ".";

    // variable primitive types
    // Ptype.NONE      IMMEDIATE - an array or a struct
    // Ptype.EMPTY     IMMEDIATE - type "empty" or no type - 0 words
    // Ptype.NULL      QUEUE      - type "null" for queue of width 0
    //                 CLOCK     - no clock domain
    //                 IMMEDIATE -  type "->" null pointer
    //                 i.e. "null" is a type and null is a value
    public enum Ptype {
        UINT       ("uint"),
        INT        ("int"),
        UFIXED     ("ufixed"),
        FIXED      ("fixed"),
        FLOAT      ("float"),
        BITS       ("bits"),
        LOG        ("log"),
        ENUM       ("enum"),
        STR        ("str"),
        PTR        ("->"),
        TYPE       ("type"),
        MAP        ("map"),
        LIST       ("list"),
        CLASS      ("class"),
        FILE       ("file"),
        EMPTY      ("empty"),
        NULL       ("null"),
        NONE       ("no type");
        private final String    name;
        Ptype (String n) {name = n;}
        public String typename () {return(name);}
        public WordSpec getWordSpec() {
            switch (this) {
            case BITS:
                return(WordSpec.BITS);
            case UINT:
                return(WordSpec.UINT);
            case INT:
                return(WordSpec.INT);
            case FLOAT:
                return(WordSpec.FLOAT);
            case LOG:
                return(WordSpec.ILOG);
            case STR:
                return(WordSpec.STR);
            case UFIXED:    // FUDGE - only used for a zero constant in EXPRNode
                return(WordSpec.UINT);
            case FIXED:     // FUDGE - only used for a zero constant in EXPRNode
                return(WordSpec.INT);
            default:
                throw new ExEx("Ptype system error!");
            }
        }
        public boolean isSigned () {
            return((this == Ptype.INT) || (this == Ptype.FIXED));
        }
    }      

    // class, module, procedure or function pointer types
    // UCPTR    user class pointer
    // IMPTR    inbuilt module pointer
    // UMPTR    user module pointer
    // IPPTR    inbuilt procedure pointer
    // UPPTR    user procedure pointer
    // IFPTR    inbuilt function pointer
    // UFPTR    user function pointer
    public enum PtrType {NONE, UCPTR, IMPTR, UMPTR, IPPTR, UPPTR, IFPTR, UFPTR}

    // Variable modes.
    public enum Mode {
        IMMEDIATE   ("immediate"),
        VALUE       ("value"),
        STATIC      ("static"),
        QUEUE       ("queue"),
        SELECTVALUE ("selectvalue"),
        PRIORITY    ("priority"),
        CMEMORY     ("cmemory"),
        RMEMORY     ("rmemory"),
        INPUT       ("input"),
        OUTPUT      ("output"),
        CLOCK       ("clock");
        private final String name;
        Mode (String n) {name = n;}
        public String modename () {return(name);}
    }

    // Tree operators.
    public enum TreeOp {
        MIN         ("unary -"),     // unary -
        BITNOT      ("unary ~"),     // unary ~
        LOGNOT      ("unary !"),     // unary !
        RAV         ("unary <"),     // unary < - queue read availability
        WAV         ("unary >"),     // unary > - queue write availability
        EXAM        ("unary ?"),     // unary ? - queue examine
        ADD         ("binary +"),    // binary +
        SUB         ("binary -"),    // binary -
        MUL         ("binary *"),    // binary *
        DIV         ("binary /"),    // binary /
        REM         ("binary %"),    // binary %
        LS          ("binary <<"),   // binary <<
        RS          ("binary >>"),   // binary >>
        LR          ("binary ^<<"),  // binary ^<<)
        RR          ("binary >>^"),  // binary >>^)
        LT          ("binary <"),    // binary <
        GT          ("binary >"),    // binary >
        LE          ("binary <="),   // binary <=
        GE          ("binary >="),   // binary >=
        EQ          ("binary =="),   // binary ==
        NE          ("binary !="),   // binary !=
        BITAND      ("binary &"),    // binary &      A AND B
//        BITNAND     ("binary ~(&)"), // binary ~(&)   A NAND B
//        _BITAND     ("binary ~ &"),  // binary ~ &    ~A AND B
//        BITAND_     ("binary & ~"),  // binary & ~    A AND ~B
        BITOR       ("binary |"),    // binary |      A OR B
//        BITNOR      ("binary ~(|)"), // binary ~(|)   A NOR B
//        _BITOR      ("binary ~ |"),  // binary ~ |    ~A OR B
//        BITOR_      ("binary | ~"),  // binary | ~    A OR ~B
        BITXOR      ("binary ^"),    // binary ^      A XOR B
//        BITXNOR     ("binary ~(^)"), // binary ~(^)   A XNOR B
        LOGAND      ("binary &&"),   // binary &&
        LOGXOR      ("binary ^^"),   // binary ^^
        LOGOR       ("binary ||"),   // binary ||
        CONDIT      ("ternary ?:"),  // ternary ?:
        ASSIGN      ("assign"),      // special case - used for assignment
        ADDSUB      ("ternary +-");  // special case - used for simple ALU
        private final String name;
        TreeOp (String n) {name = n;}
        public String opname () {return(name);}
    };
    
    // Subscript/field element type.
    public enum SFType {
        NONE,      //        no subscript            
        NULL,      // []     empty subscript             
        IMSUB,     // [i]    immediate subscript      
        IMSUBS,    // [i..j] immediate subscript range
        TARSUB,    // [t]    target subscript
        KEY,       // [key]  map key
        FIELD      // .field struct field name  
    };        
    
    // Flags to indicate that a VarNode has a ++ or -- applied to it.
    public enum Flag {NONE, PREINCR, PREDECR, POSTINCR, POSTDECR};
    
    
    // execute method return values
    public enum EXECR {NONE, BREAK, CONTINUE, RETURN};
    
    // Block type
    public enum Btype {
        UNDEF       ("undef"),    // not yet defined
        IF          ("if"),       // if (..) true or false code block
        WHILE       ("while"),    // while (..) code block
        DOWHILE     ("dowhile"),  // do {...} while  (..) code block
        FOR         ("for"),      // for (;;) code block
        FORVAR      ("forvar"),   // for (;;) loop variable scope
        FSRC        ("fsrc"),     // source block (transparent)
        FMOD        ("fmod"),     // file module body
        GROUP       ("class"),    // class body
        INIT        ("initial"),  // class initialisation
        NMOD        ("nmod"),     // named module body
        ILMOD       ("ilmod"),    // in-line module body
        PROC        ("proc"),     // procedure body
        FUNC        ("func"),     // function body
        CASE        ("case"),     // case ..: ... within switch
        WHEN        ("when"),     // when (..) true or false code block 
        SEQ         ("seq"),      // seq {...}
        PAR         ("par"),      // par {...}
        SYNC        ("sync"),     // sync {...}
        SYNCWHEN    ("syncwhen"), // sync when () {...}
        WAITPRI     ("waitpri"),  // waitpri () ...
        EXCEP       ("excep"),    // except () {...}
        PETRINET    ("petrinet"); // petrinet {...... {...} ....}
        private final String name;
        Btype (String n) {name = n;}
        public String bname () {return(name);}
    }                
    
    // Assignment type
    public enum AST {
        IMASS           ("="),   // immediate assign
        IMPLUSASS       ("+="),  // immediate add assign
        IMMINASS        ("-="),  // immediate subtract  assign
        IMMULASS        ("*="),  // immediate multiply assign
        IMDIVASS        ("/="),  // immediate divide assign
        IMREMASS        ("%="),  // immediate remainder assign
        IMBANDASS       ("&="),  // immediate bit AND assign
        IMBORASS        ("|="),  // immediate bit OR assign
        IMBXORASS       ("^="),  // immediate bit XOR assign
        IMLANDASS       ("&&="), // immediate logical AND assign
        IMLORASS        ("||="), // immediate logical OR  assign
        IMLXORASS       ("^^="), // immediate logical XOR assign
        IMLSASS         ("<<="), // immediate left shift assign
        IMRSASS         (">>="), // immediate right shift assign
        VALPRI          (":= "), // value, priority, output or clock assignment
        SELVAL          ("|-"),  // selectvalue assignment
        STATICASS       ("<-"),  // static assignment
        STATICADDASS    ("+<-"), // static add assign
        STATICSUBASS    ("-<-"), // static subtract assign
        STATICMULASS    ("*<-"), // static multiply assign
        QUEUE            ("<<-"); // queue assignment
        private final String name;        
        AST (String n) {name = n;}        
        public String assname () {return(name);}
    };

    // clock signal mode for TDEVars
    // Most TDEVars are not clocks, hence have clock mode NOT
    // Clock signals, including intermediate clock signals between inputs,
    // DLLs. buffers etc. are POS by default.
    // Negative edge clocks created by inbuilt procedure negclock() are NEG.
    // NOT  signal is not a clock
    // POS  signal is a +ve edge clock
    // NEG  signal is a -ve edge clock
    public enum ClkType {NOT, POS, NEG}

    // Token types for LUT expression parsing
    public enum Ttype {ARG, CON, AND, OR, XOR, NOT, LPAR, RPAR, END, ERR};
    
    // Variable scope context
    public enum Context {
        GLOBAL      ("global"),     // global context
        FILE        ("file"),       // file context
        GROUP       ("class"),      // class instance variable context
        CALLING     ("calling"),    // calling context
        LOCAL       ("local"),      // current module, procedure or function context
        ILMOD       ("ilmod"),      // in-line module context
        DEFAULT     ("default");    // no context specified
        private final String name;
        Context (String n) {name = n;}
        public String contextname () {return(name);}
    };
    
    // Clock allocation events
    public enum Calloc {
        ERROR           ("SYSTEM ERROR!"),
        RESET           ("reset"),
        ASSIGN          ("assignment"),
        VALUE           ("evaluate"),
        WHEN            ("when"),
        BRANCH          ("branch"),
        INPUT           ("input"),
        EXTERN          ("external"),
        DOMAIN          ("attribute domain()"),
        READDOMAIN      ("attribute readdomain()"),
        WRITEDOMAIN     ("attribute writedomain()"),
        ISREAD          ("function isread()"),
        ISWRITTEN       ("function iswritten()"),
        WASWRITTEN      ("function waswritten()"),
        IMPORT          ("import"),
        QUEUEAVAIL      ("<queue or >queue"),
        PETRINET        ("petrinet"),
        WAITPRI         ("waitpri"),
        PRIUNLOCK       ("priunlock");
        private final String name;
        Calloc (String n) {name = n;}
        public String event () {return(name);}
    }
    
    // Attribute check result.
    public enum ACT {OK, DONE, UNRECOGNISED, WRONG_TYPE};
    
    // Unassigned output behaviour.
    public enum UOB {GND, VCC, NC, FATAL};
}
