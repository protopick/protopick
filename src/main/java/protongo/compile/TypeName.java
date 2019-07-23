package protongo.compile;

import protongo.parser.ProtoParserConstants;
import protongo.parser.ProtoParserContext;
import protongo.parser.Token;

/** This represents a Message, sub-sub...Message, an Enum... */
public class TypeName {
    public final TypeNameUse use;
    /*  A string (including empty), if
        - relevant and if
        - parsed already, or if assigned by the parser as default package.
        Null if not relevant, or not parsed/assigned. */
    private final String packageName;
    private final String name;
    private final TypeName parentOrContext;
    /** "kind" (token.kind) of newTypeToken passed to the constructor, if it were non-null. Otherwise this is -1. */
    private final int newTypeTokenKind;

    /*
      @TODO Run against .proto, then remove this comment line: It may be null for usages that we're not interested in (for example other usages of ClassPart BNF rule). */
    private TypeName (TypeNameUse givenUse, Token newTypeToken, TypeName givenParent, String givenPackage, String givenName) {
        use= givenUse;
        parentOrContext = givenParent;
        packageName = givenPackage;
        if (givenName==null || givenName.isEmpty())
            throw new IllegalArgumentException("Type name must not be empty.");
        name = givenName;
        if ( (newTypeToken!=null) != use.definesNewType() )
            throw new IllegalArgumentException("Param newTypeToken can be used when, and only when, defining not a new field, but a new type: " +name);
        if (use.definesNewType() && name.contains("."))
            throw new IllegalArgumentException("When defining not a new field, but a new type, the name must not contain a dot. However, it did: " +name);
        if (newTypeToken!=null)
            newTypeTokenKind = newTypeToken.kind;
        else {
            newTypeTokenKind = -1;
        }
        // We can't check packageName, since we can't require the package name in .proto.
        if (parentOrContext ==null && newTypeTokenKind !=ProtoParserConstants.MESSAGE_TKN )
            throw new IllegalArgumentException("Top-level type must be a message, not a " +newTypeToken.image);
    }

    /** A name of a new type defined by the user at the top (package) level.
     * @param newTypeToken Token ("message", "enum"...) that we're defining a new type for. If, instead, we're
     * defining a field only, then use a different constructor that doesn't accept a Token parameter.
     * @param givenPackage pass "packageName" variable from BNF grammar rules */
    public TypeName (Token newTypeToken, String givenPackage, String givenName) {
        this (TypeNameUse.TYPE_TOP_LEVEL, newTypeToken, null, givenPackage, givenName);
    }

    /** A name of a new type defined by the user. Not that the top (package) level, but as a part of another message.
     * @param newTypeToken See another public constructor that accepts a Token and other parameters.
     *  @param givenParent The immediate outer message. It must be non-null; otherwise use the other constructor.
     * */
    public TypeName (Token newTypeToken, TypeName givenParent, String givenName) {
        this (TypeNameUse.TYPE_LOWER_LEVEL, newTypeToken, givenParent, null, givenName);
    }

    /** A flexible constructor of both top-level and lower-level names for defining new types ("message", "enum"...).
     *  You can call it with givenPackage regardless of the level, and it ignores it for lower-level names.
     * @param givenPackage pass "packageName" variable from BNF grammar rules
     * */
    public TypeName (Token newTypeToken, String givenPackage, TypeName givenParent, String givenName) {
        this (givenParent==null ? TypeNameUse.TYPE_TOP_LEVEL : TypeNameUse.TYPE_LOWER_LEVEL,
              newTypeToken,
              givenParent,
              givenParent==null ? givenPackage : null,
              givenName);
    }

    /** For recording *potentially* relative references to user-defined types.
     * @param givenPackage pass "packageName" variable from BNF grammar rules
     * @param givenName May contain dot-separated parts, or just one name.
     * */
    public TypeName (String givenPackage, TypeName givenContext, String givenName) {
        this (TypeNameUse.FIELD, null, givenContext, givenPackage, givenName);
    }

    /** Use when defining a new field whose type is a simple (built-in) Protobuf type.
     * @param simpleType Token of the built-in Protobuf type. NOT the same meaning as newTypeToken in the other constructors. */
    public TypeName (Token simpleType) {
        this (TypeNameUse.FIELD_SIMPLE, null, null, null, simpleType.image);
        if (simpleType.kind!=ProtoParserConstants.TYPE_TKN)
            throw new IllegalArgumentException("Must pass a built-in type, not: " +simpleType);
    }

    public String fullName() {
        if (use.mayBeRelative())
            throw new UnsupportedOperationException("Referral type names don't have full name defined.");
        if (parentOrContext != null)
            return parentOrContext.fullName() + '.' +name;
        else if (packageName!=null && !packageName.isEmpty())
            return packageName + '.' +name;
        else
            return name;
    }

    /* Situation: We're parsing a message. One of its fields refers to another type (a message, enum...).
       That type reference is either:
       1. Qualified (with a dot or several) type with a package name: name.of.a.package.nameOfType.[nameOfSubtype...]
       2. Qualified (with a dot or several, or none) sub-message: nameOfAnotherMessage[.nameOfItsInnerMessage...]
       where nameOfAnotherMessage is a direct child of any ancestor of the current message (immediate parent of the field being defined),
       up to and including the top level (the current package) as the farthest such ancestor.
       That way this includes any matching types from the top level of the current package, too.
       If there's no current package, #2 assumes it to be an empty string.
       #1 has precedence over #2, because if there's such a conflict, #2 can work around by providing
       a path from one level higher. If it reaches the package level, and if there's still a conflict,
       then the user can work around by adding a package prefix to #2 (effectively making it another #1).
       <br/>
       While parsing, even if that other type is in the same file, we don't want to start parsing it right now.
       We've either already parsed it, or we will parse it later, in this same ProtoParser instance.
       Hence, BNF rules don't resolve, they only register Type instances with ProtoParserContext. We call this resolveFieldType() only later, once all BNF rules were processed.
       (Also, the same package may be across multiple files => involving multiple parsers...)
       <br/>
       Do NOT use until the whole input gets parsed. Otherwise this can't identify "relative" (sub-message) names.
       <br/>
       Only use it from the same Thread as the one that created the parser.
    */
    public TypeName resolveFieldType() {
        if (!use.mayBeRelative())
            throw new UnsupportedOperationException("You can't call resolve() for identifiers that define new types. Only call it for identifiers that define non-simple fields.");
        // @TODO prepopulate: com.google.Empty etc.
        // 1. Try this,name as a combination of a package name and a (potentially multi-level) type name within that package.
        ProtoParserContext context= ProtoParserContext.perThread();
        if (context.newTypes.containsKey(name))
            return context.newTypes.get(name);
        //final String packageParts[]= packageName.split(".");
        // TODO TEST: NO SPLITTING
        //String subPackage= packageName;
        //    int i= subPackage.lastIndexOf(".");

        // 2. Try this name as a relative to (if any) outer message. Start with the immediate parent, then grandparent, great-grandparent... up to the top level message.
        // nameOfAnotherMessage[.nameOfItsInnerMessage...]
        // Start from the innermost context - the closest has the highest precedence.
        //final String nameParts[]= parentOrContext.fullName().split(".");
        String subContext= parentOrContext.fullName();
        while (true) {
            String candidate= subContext+name;
            if (context.newTypes.containsKey(candidate))
                return context.newTypes.get(candidate);
            int i= subContext.lastIndexOf(".");
            if (i>0) {
                subContext= subContext.substring(0, i);
            }
            else
                return null;
        }
    }

    /** Equality is based on fullName(). That way you can create an instance with a dot-separated multi-level name in one go,
     * and it compares well to a cascaded instance, too. */
    @Override
    public boolean equals(Object other) {
        return other!=null && fullName().equals( ((TypeName)other).fullName() );
    }

    public int hashCode() {
        return fullName().hashCode();
    }
}
