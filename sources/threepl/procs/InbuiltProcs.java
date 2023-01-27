package threepl.procs;

import java.util.Map;
import java.util.TreeMap;

import threepl.exceptions.ExEx;


/**
 * This class is only instantiated once as a static class in main.
 * It is a map of all inbuilt procedures.
 */
public class InbuiltProcs {
    static TreeMap<String,InbuiltProc>  procs;

    /**
     * Construct the map of all inbuilt procedures.
     * Each inbuilt procedure has an identifier and
     * a class with an <b>execute</b> method which implements the procedure.
     * This class contains a map whose indices are the procedure identifiers
     * and whose values are the procedure classes.
     * The explicit procedure identifiers and associated classes are coded
     * into the body of this constructor.
     */
    public InbuiltProcs () {
        procs = new TreeMap<String, InbuiltProc>();

        procs.put("addmodeattribute", new AddModeAttributeProc());
        procs.put("append", new AppendProc());
        procs.put("appendall", new AppendAllProc());
        procs.put("assert", new AssertProc());
        procs.put("argadjust", new ArgAdjustProc());
        procs.put("attributeprocedure", new AttributeProcedureProc());
        procs.put("attributes", new AttributesProc());
        procs.put("change", new ChangeProc());
        procs.put("class", new ClassProc());
        procs.put("clock", new ClockProc());
        procs.put("clockfromexpr", new ClockFromExprProc());
        procs.put("close", new CloseProc());
        procs.put("connect", new ConnectProc());
        procs.put("cmemory", new CmemoryProc());
        procs.put("cmemorywrite", new CmemoryWriteProc());
        procs.put("designname", new DesignNameProc());
        procs.put("directive", new DirectiveProc());
        procs.put("divrem", new DivRemProc());
        procs.put("element", new ElementProc());
        procs.put("elementclockinput", new ElementClockInputProc());
        procs.put("elementend", new ElementEndProc());
        procs.put("elementinput", new ElementInputProc());
        procs.put("elementoutput", new ElementOutputProc());
        procs.put("elementproperty", new ElementPropertyProc());
        procs.put("elementtsoutput", new ElementTSOutputProc());
        procs.put("emptyvalue", new EmptyValueProc());
        procs.put("enum", new EnumProc());
        procs.put("enumv", new EnumvProc());
        procs.put("exec", new ExecProc());
        procs.put("exit", new ExitProc());
        procs.put("export", new ExportProc());
        procs.put("external", new ExternalProc());
        procs.put("file", new FileProc());
        procs.put("float", new FloatProc());
        procs.put("fprintf", new FprintfProc());
        procs.put("fscanf", new FscanfProc());
        procs.put("ident", new IdentProc());
        procs.put("import", new ImportProc());
//        procs.put("includedir", new IncludeDirProc());
        procs.put("input", new InputProc());
        procs.put("int", new IntProc());
        procs.put("log", new LogProc());
        procs.put("log", new LogProc());
        procs.put("liststacks", new ListStacksProc());
        procs.put("nop", new NopProc());
        procs.put("negclock", new NegClockProc());
        procs.put("msg", new MsgProc());
        procs.put("open", new OpenProc());
        procs.put("output", new OutputProc());
        procs.put("put", new PutProc());
        procs.put("part", new PartProc());
        procs.put("putall", new PutAllProc());
        procs.put("queue", new QueueProc());
        procs.put("printf", new PrintfProc());
        procs.put("println", new PrintlnProc());
        procs.put("priority", new PriorityProc());
        procs.put("priunlock", new PriUnlockProc());
        procs.put("pulse", new PulseProc());
        procs.put("ref", new RefProc());
        procs.put("remove", new RemoveProc());
        procs.put("removedirective", new RemoveDirectiveProc());
        procs.put("rpt", new RptProc());
        procs.put("reset", new ResetProc());
        procs.put("rmemory", new RmemoryProc());
        procs.put("rmemoryread", new RmemoryReadProc());
        procs.put("rmemorywrite", new RmemoryWriteProc());
//        procs.put("select", new SelectProc());
//        procs.put("selecta", new SelectAProc());
        procs.put("scanf", new ScanfProc());
        procs.put("selectvalue", new SelectValueProc());
        procs.put("setdirective", new SetDirectiveProc());
        procs.put("shell", new ShellProc());
//        procs.put("simread", new SimReadProc());
//        procs.put("simwrite", new SimWriteProc());
        procs.put("special", new SpecialProc());
        procs.put("sprintf", new SprintfProc());
        procs.put("sscanf", new SscanfProc());
        procs.put("start", new StartProc());
        procs.put("statemachine", new StateMachineProc());
        procs.put("static", new StaticProc());
        procs.put("stop", new StopProc());
        procs.put("str", new StrProc());
        procs.put("struct", new StructProc());
        procs.put("trace", new TraceProc());
        procs.put("type", new TypeProc());
        procs.put("texit", new TexitProc());
        procs.put("useclock", new UseClockProc());
        procs.put("value", new ValueProc());
        procs.put("var", new VarProc());
        procs.put("writeln", new WriteLnProc());
    }
    
    /**
     * Search the inbuilt procedure map for a procedure.
     * @param   id is the procedure identifier
     * @return  the inbuilt procedure
     */
    public InbuiltProc findProc (String id) {
        return(procs.get(id));
    }
    
    /**
     * Check if an inbuilt procedure with the given identifier exists.
     * @param id is the identifier
     * @return  true if the inbuilt procedure exists
     */
    public static boolean procDefined(String id) {
        return(procs.containsKey(id));
    }
    
    /**
     * Search the inbuilt procedure map for the identifier of a procedure.
     * This is only used to recover the procedure identifier when invoking
     * execution via a pointer.
     * @param   p is the procedure
     * @return  the procedure identifier
     */
    public static String findProcId (InbuiltProc p) {
        for (Map.Entry<String,InbuiltProc> me: procs.entrySet()) {
            if (p.equals(me.getValue()))
                return(me.getKey());
        }
        throw new ExEx("InbuiltProcs: SYSTEM ERROR");
    }
}
