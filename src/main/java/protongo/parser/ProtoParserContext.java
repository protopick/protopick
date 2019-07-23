package protongo.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import protongo.compile.TypeName;
//import protongo.compile.TypeRelativeName;

/** Load parser(s) for included file(s).
 * This is not memory efficient. That's not the goal. */
public final class ProtoParserContext {
    public final List<String> importPaths= new ArrayList<>();

    private final Map<String, TypeName> newTypesMutable= new HashMap<>();

    /**"New types" mean type names used not for fields, but for types defined by the user ("message", "enum").
     * Full type name (including protobuf package, if any) -> Type. We add them in by addType(TypeName) as we parse. */
    public final Map<String, TypeName> newTypes= Collections.unmodifiableMap(newTypesMutable);

    public void addNewDefinedType( TypeName type ) {
        final String name = type.fullName();
        if (!type.use.definesNewType())
            throw new IllegalArgumentException("Don't add identifiers that define new fields: " +name+ ". Only add those that define new types.");
        if (newTypesMutable.containsKey(name))
            throw new IllegalArgumentException("Type with name " +name+ " has been registered already.");
        newTypesMutable.put(name, type);
    }

    /** Including the path of the "root" file. This has to be a list, not a set, because `protoc`
     * applies the path folders in a given order. */
    public final List<String> includePaths= new ArrayList<>();

    /** Message name => HandlingInstruction */
    public final Map<TypeName, HandlingInstruction> instructions= new HashMap<>();

    /* ------ Registration-specific ----- */
    private static final ThreadLocal<ProtoParserContext> threadContext= ThreadLocal.withInitial( ()-> new ProtoParserContext());

    public static ProtoParserContext perThread() {
        return threadContext.get();
    }

    public final List<ProtoParser> parsers = new ArrayList<>();

    /* Called from ProtoParser class only (see ProtoParser.jjt). Hence package-access is enough.
    * We use one parser per file. The root parser creates extra parsers
    * for "import" clauses. Each parser registers itself, but that does not
    * reset/cleanup this ProtoParserContext instance.
    * */
    static void register (ProtoParser parser) {
        if (perThread().parsers.contains(parser)) {
            throw new IllegalStateException("Parser already registered.");
        }
        perThread().parsers.add (parser);
    }

    // This should be called only by ThreadLocal.withInitial(..) mechanism, which will then set the ThreadLocal binding.
    private ProtoParserContext() {
        if (perThread()!=null) {
            throw new IllegalStateException("ParserContext already registered for this Thread. Call unregisterContext() first.");
        }
    }

    public static void unregisterContext() {
        if (perThread()==null) {
            throw new IllegalStateException("No ParserContext registered for this Thread.");
        }
        threadContext.set (null);
    }
}