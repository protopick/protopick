package protongo.compile;

/** For new types defined by the user. Not for a reference to  atype used for a field. */
public final class TypeNameDefine extends TypeName {
    /*
          @TODO Run against .proto, then remove this comment line: It may be null for usages that we're not interested in (for example other usages of ClassPart BNF rule). */
    TypeName (TypeNameUse givenUse, Token newTypeToken, TypeName givenParent, String givenPackage, String givenName) {
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
    TypeName (Token newTypeToken, String givenPackage, String givenName) {
        this (TypeNameUse.TYPE_TOP_LEVEL, newTypeToken, null, givenPackage, givenName);
    }

    /** A name of a new type defined by the user. Not that the top (package) level, but as a part of another message.
     * @param newTypeToken See another public constructor that accepts a Token and other parameters.
     *  @param givenParent The immediate outer message. It must be non-null; otherwise use the other constructor.
     * */
    TypeName (Token newTypeToken, TypeName givenParent, String givenName) {
        this (TypeNameUse.TYPE_LOWER_LEVEL, newTypeToken, givenParent, null, givenName);
    }

    /** A flexible constructor of both top-level and lower-level names for defining new types ("message", "enum"...).
     *  You can call it with givenPackage regardless of the level, and it ignores it for lower-level names.
     * @param givenPackage pass "packageName" variable from BNF grammar rules
     * */
    public TypeName(Token newTypeToken, String givenPackage, TypeName givenParent, String givenName) {
        this (givenParent==null ? TypeNameUse.TYPE_TOP_LEVEL : TypeNameUse.TYPE_LOWER_LEVEL,
              newTypeToken,
              givenParent,
              givenParent==null ? givenPackage : null,
              givenName);
    }
}
