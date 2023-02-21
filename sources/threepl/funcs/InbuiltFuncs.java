package threepl.funcs;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import threepl.exceptions.ExEx;

/**
 * This class is only instantiated once as a static class in main.
 * It is a map of all inbuilt functions.
 */
public class InbuiltFuncs {
    static TreeMap<String,InbuiltFunc>  funcs;

    /**
     * Construct the map of all inbuilt functions.
     * Each inbuilt function has an identifier and
     * a class with a <b>getVal</b> method which implements the function.
     * This class contains a map whose indices are the function identifiers
     * and whose values are the function classes.
     * The explicit function identifiers and associated classes are coded
     * into the body of this constructor.
     *
     * Commented-out functions are obscure, obsolete, illconsidered or
     * were temporarily expedient.
     */
    public InbuiltFuncs () {
        funcs = new TreeMap<String, InbuiltFunc>();
        funcs.put("abs", new AbsFunc());
        funcs.put("accept", new AcceptFunc());
        funcs.put("acos", new AcosFunc());
        funcs.put("addsub", new AddSubFunc());
        funcs.put("arglist", new ArgListFunc());
        funcs.put("arraytostr", new ArrayToStrFunc());
        funcs.put("arraytype", new ArrayTypeFunc());
        funcs.put("asin", new AsinFunc());
        funcs.put("atan", new AtanFunc());
        funcs.put("atan2", new Atan2Func());
        funcs.put("attribute", new AttributeFunc());
        funcs.put("attributes", new AttributesFunc());
        funcs.put("biasedexponent", new BiasedExponentFunc());
        funcs.put("biasedexponentwidth", new BiasedExponentWidthFunc());
        funcs.put("bitsfor", new BitsForFunc());
        funcs.put("cast", new CastFunc());
        funcs.put("ceil", new CeilFunc());
        funcs.put("cmemoryread", new CmemoryReadFunc());
//        funcs.put("componentpointer", new ComponentPointerFunc());
//        funcs.put("componenttype", new ComponentTypeFunc());
        funcs.put("cos", new CosFunc());
        funcs.put("copy", new CopyFunc());
        funcs.put("cspan", new CSpanFunc());
        funcs.put("currentclock", new CurrentClockFunc());
        funcs.put("currentelement", new CurrentElementFunc());
        funcs.put("dimension", new DimensionFunc());
        funcs.put("deflate", new DeflateFunc());
        funcs.put("dimensions", new DimensionsFunc());
        funcs.put("directive", new DirectiveFunc());
        funcs.put("directives", new DirectivesFunc());
        funcs.put("direxists", new DirExistsFunc());
        funcs.put("domain", new DomainFunc());
        funcs.put("dutycycle", new DutyCycleFunc());
        funcs.put("elapsedtime", new ElapsedTimeFunc());
        funcs.put("endswith", new EndsWithFunc());
        funcs.put("entries", new EntriesFunc());
        funcs.put("env", new EnvFunc());
        funcs.put("exists", new ExistsFunc());
        funcs.put("exp", new ExpFunc());
        funcs.put("find", new FindFunc());
//        funcs.put("first", new FirstFunc());
        funcs.put("floatisnegative", new FloatIsNegativeFunc());
        funcs.put("floor", new FloorFunc());
        funcs.put("format", new FormatFunc());
        funcs.put("frequency", new FrequencyFunc());
        funcs.put("get", new GetFunc());
//        funcs.put("getfield", new GetFieldFunc());
//        funcs.put("hasfield", new HasFieldFunc());
        funcs.put("hasnext", new HasNextFunc());
        funcs.put("hasqueuereads", new HasQueueReadsFunc());
        funcs.put("hasprev", new HasPrevFunc());
        funcs.put("head", new HeadFunc());
        funcs.put("identifier", new IdentifierFunc());
//        funcs.put("imtotargtype", new ImToTargTypeFunc());
        funcs.put("includedirs", new IncludeDirsFunc());
        funcs.put("inttobinstring", new IntToBinStringFunc());
        funcs.put("inttohexstring", new IntToHexStringFunc());
        funcs.put("isnull", new IsNullFunc());
        funcs.put("isempty", new IsEmptyFunc());
        funcs.put("isread", new IsReadFunc());
        funcs.put("issigned", new IsSignedFunc());
        funcs.put("isstruct", new IsStructFunc());
        funcs.put("istargconst", new IsTargConstFunc());
        funcs.put("iswritten", new IsWrittenFunc());
//        funcs.put("last", new LastFunc());
        funcs.put("listattributes", new ListAttributesFunc());
        funcs.put("listvars", new ListVarsFunc());
        funcs.put("location", new LocationFunc());
        funcs.put("log", new LogFunc());
        funcs.put("mantissa", new MantissaFunc());
        funcs.put("mantissawidth", new MantissaWidthFunc());
        funcs.put("matched", new MatchedFunc());
        funcs.put("match", new MatchFunc());
        funcs.put("max", new MaxFunc());
        funcs.put("maxvalue", new MaxValueFunc());
        funcs.put("memoryaddresstype", new MemoryAddressTypeFunc());
        funcs.put("memorydatatype", new MemoryDataTypeFunc());
        funcs.put("min", new MinFunc());
        funcs.put("minvalue", new MinValueFunc());
        funcs.put("mod", new ModFunc());
        funcs.put("mode", new ModeFunc());
//        funcs.put("next", new NextFunc());
        funcs.put("nullarg", new NullArgFunc());
//        funcs.put("numfields", new NumFieldsFunc());
        funcs.put("numinargs", new NumInArgsFunc());
        funcs.put("numoutargs", new NumOutArgsFunc());
        funcs.put("numwords", new NumWordsFunc());
        funcs.put("offset", new OffsetFunc());
        funcs.put("ord", new OrdFunc());
        funcs.put("period", new PeriodFunc());
        funcs.put("pow", new PowFunc());
        funcs.put("portdomain", new PortDomainFunc());
//        funcs.put("prev", new PrevFunc());
        funcs.put("prevclock", new PrevClockFunc());
        funcs.put("prielse", new PriElseFunc());
        funcs.put("primtypestring", new PrimTypeStringFunc());
        funcs.put("queuedepth", new QueueDepthFunc());
        funcs.put("queueisempty", new QueueIsEmptyFunc());
        funcs.put("queuemaxdepth", new QueueMaxDepthFunc());
        funcs.put("queuemindepth", new QueueMinDepthFunc());
        funcs.put("queuereadstatus", new QueueReadStatusFunc());
        funcs.put("queuespaces", new QueueSpacesFunc());
        funcs.put("queuewords", new QueueWordsFunc());
        funcs.put("queuewritestatus", new QueueWriteStatusFunc());
        funcs.put("random", new RandomFunc());
        funcs.put("readdomain", new ReadDomainFunc());
        funcs.put("replace", new ReplaceFunc());
        funcs.put("replaceall", new ReplaceAllFunc());
        funcs.put("resetoutput", new ResetOutputFunc());
        funcs.put("round", new RoundFunc());
        funcs.put("rmemoryout", new RmemoryOutFunc());
        funcs.put("sample", new SampleFunc());
//        funcs.put("select", new SelectFunc());
        funcs.put("selecta", new SelectAFunc());
        funcs.put("signum", new SignumFunc());
        funcs.put("sin", new SinFunc());
        funcs.put("size", new SizeFunc());
        funcs.put("span", new SpanFunc());
        funcs.put("split", new SplitFunc());
        funcs.put("sqrt", new SqrtFunc());
        funcs.put("stacktrace", new StackTraceFunc());
        funcs.put("startswith", new StartsWithFunc());
        funcs.put("strtoarray", new StrToArrayFunc());
        funcs.put("strtoint", new StrToIntFunc());
        funcs.put("strtofloat", new StrToFloatFunc());
        funcs.put("strtotype", new StrToTypeFunc());
        funcs.put("strlen", new StrLenFunc());
        funcs.put("strpos", new StrPosFunc());
        funcs.put("strrpos", new StrrPosFunc());
        funcs.put("substitute", new SubstituteFunc());
        funcs.put("substring", new SubstringFunc());
        funcs.put("tail", new TailFunc());
        funcs.put("tan", new TanFunc());
        funcs.put("targconsttoimmed", new TargConstToImmedFunc());
        funcs.put("targtoimtype", new TargToImTypeFunc());
        funcs.put("time", new TimeFunc());
        funcs.put("timestring", new TimeStringFunc());
        funcs.put("toarray", new ToArrayFunc());
        funcs.put("todegree", new ToDegreeFunc());
        funcs.put("tolower", new ToLowerFunc());
        funcs.put("toradian", new ToRadianFunc());
        funcs.put("tostring", new ToStringFunc());
        funcs.put("toupper", new ToUpperFunc());
        funcs.put("trunc", new TruncFunc());
        funcs.put("type", new TypeFunc());
        funcs.put("typestring", new TypeStringFunc());
        funcs.put("typeisequal", new TypeIsEqualFunc());
        funcs.put("typeval", new TypeValFunc());
        funcs.put("typevalstring", new TypeValStringFunc());
        funcs.put("used", new UsedFunc());
        funcs.put("varexists", new VarExistsFunc());
        funcs.put("varlist", new VarListFunc());
        funcs.put("wasassigned", new WasAssignedFunc());
        funcs.put("waswritten", new WasWrittenFunc());
        funcs.put("width", new WidthFunc());    
        funcs.put("words", new WordsFunc());    
        funcs.put("writedomain", new WriteDomainFunc());
        
        // file functions
        funcs.put("filecanread", new FileCanReadFunc());
        funcs.put("filecanwrite", new FileCanWriteFunc());
        funcs.put("filedelete", new FileDeleteFunc());
        funcs.put("fileexists", new FileExistsFunc());
        funcs.put("filegetparent", new FileGetParentFunc());
        funcs.put("fileisdir", new FileIsDirFunc());
        funcs.put("fileisfile", new FileIsFileFunc());
        funcs.put("filelastmod", new FileLastModFunc());
        funcs.put("filelength", new FileLengthFunc());
        funcs.put("filelist", new FileListFunc());
        funcs.put("filelistdirs", new FileListDirsFunc());
        funcs.put("filelistfiles", new FileListFilesFunc());
        funcs.put("filemkdir", new FileMkDirFunc());
        funcs.put("filerename", new FileRenameFunc());
        funcs.put("filesetreadonly", new FileSetReadOnlyFunc());
        funcs.put("hadeof", new HadEOFFunc());
        funcs.put("readbyte", new ReadByteFunc());
        funcs.put("readln", new ReadLnFunc());
    }

    /**
     * Search the inbuilt function map for a function.
     * @param   id is the function identifier
     * @return  the inbuilt function
     */
    public InbuiltFunc findFunc (String id) {
        return(funcs.get(id));
    }
    
    /**
     * Check if an inbuilt function with the given identifier exits.
     * @param id is the identifier
     * @return  true if the inbuilt function exists
     */
    public static boolean funcDefined(String id) {
        return(funcs.containsKey(id));
    }
    
    /**
     * Search the inbuilt function map for the identifier of a function.
     * This is only used to recover the function identifier when invoking
     * execution via a pointer.
     * @param   f is the function
     * @return  the function identifier
     */
    public static String findFuncId (InbuiltFunc f) {
        Set<Entry<String, InbuiltFunc>>         es = funcs.entrySet();
        for (Map.Entry<String, InbuiltFunc> me: es) {
            if (f.equals(me.getValue()))
                return(me.getKey());
        }
        throw new ExEx("InbuiltProcs: SYSTEM ERROR");
    }
}
