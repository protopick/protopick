package protongo.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Load parser(s) for included file(s).
 * This is not memory efficient. That's not the goal. */
public final class ProtoParserContext {
    public final List<String> importPaths= new ArrayList<>();

    private final Map<String, Type> typesMutable= new HashMap<>();
    /** Full type name (including protobuf package, if any) -> Type.*/
    public final Map<String, Type> types= Collections.unmodifiableMap(typesMutable);
    public void addType( Type type ) {
        final String name = type.fullName();
        if (typesMutable.containsKey(name))
            throw new IllegalArgumentException("Type with name " +name+ " has been registered already.");
        typesMutable.put(name, type);
    }

    public final Map<String, String> messageToSpecial= new HashMap<>();


    /* ------ Registration-specific ----- */
    private static final ThreadLocal<ProtoParserContext> threadContext= new ThreadLocal<>();

    static ProtoParserContext perThread() {
        return threadContext.get();
    }

    public final List<ProtoParser> parsers = new ArrayList<>();

    /* Called from ProtoParser class only (see ProtoParser.jjt). Hence package-access is enough. */
    static void register (ProtoParser parser) {
        if (perThread().parsers.contains(parser)) {
            throw new IllegalStateException("Parser already registered.");
        }
        perThread().parsers.add (parser);
    }

    public ProtoParserContext() {
        if (perThread()!=null) {
            throw new IllegalStateException("ParserContext already registered for this Thread. Call unregisterContext() first.");
        }
        threadContext.set (this);
    }

    public static void unregisterContext() {
        if (perThread()==null) {
            throw new IllegalStateException("No ParserContext registered for this Thread.");
        }
        threadContext.set (null);
    }
}