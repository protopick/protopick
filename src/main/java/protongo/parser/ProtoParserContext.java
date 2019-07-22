package protongo.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import protongo.compile.TypeFullName;
import protongo.compile.TypeRelativeName;

/** Load parser(s) for included file(s).
 * This is not memory efficient. That's not the goal. */
public final class ProtoParserContext {
    public final List<String> importPaths= new ArrayList<>();

    private final Map<String, TypeFullName> typesMutable= new HashMap<>();
    /** Full type name (including protobuf package, if any) -> Type.*/
    public final Map<String, TypeFullName> types= Collections.unmodifiableMap(typesMutable);
    public void addType( TypeFullName type ) {
        final String name = type.fullName();
        if (typesMutable.containsKey(name))
            throw new IllegalArgumentException("Type with name " +name+ " has been registered already.");
        typesMutable.put(name, type);
    }

    /** Message name => HandlingInstruction */
    public final Map<TypeFullName, HandlingInstruction> instructions= new HashMap<>();

    /* Situation: We're parsing a message. One of its fields refers to another type (a message, enum...).
       That type reference is either:
       1. Unqualified (no dot). That's a name of another type in the same package (in the same file, or a different file),
       or a name of a sub-message or enum in the current message.
       2. Qualified (with a dot or several) type with a package name: name.of.a.package.nameOfType.
       2. Qualified (with a dot or several) sub-message: nameOfAnotherMessage.nameOfItsInnerMessage...
       where nameOfAnotherMessage may contain a package name or not.
       If that other type is in the same file, we don't want to start parsing it right now. We've either already
       parsed it, or we will parse it later, in this same ProtoParser instance.
       Hence, BNF rules only register Type instances with ProtoParserContext. But they're validated and "compiled" only later.
       <br/>
       Do not use until the whole input gets parsed. Otherwise this can't identify whether "relative" (sub-message) names.
    */
    public final TypeFullName resolve(TypeFullName context, TypeRelativeName relative) {
        return null;
    }

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