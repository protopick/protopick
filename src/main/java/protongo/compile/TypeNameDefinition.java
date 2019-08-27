package protongo.compile;

import protongo.parser.Token;

/** For new types defined by the user. Not for a reference to  atype used for a field. */
public final class TypeNameDefinition extends TypeName {
/** A name of a new type defined by the user at the top (package) level.
     * @param newTypeToken Token ("message", "enum"...) that we're defining a new type for. If, instead, we're
     * defining a field only, then use a different constructor that doesn't accept a Token parameter.
     * @param givenPackage pass "packageName" variable from BNF grammar rules */
    public TypeNameDefinition (Token newTypeToken, TypeNamePackage givenPackage, String givenName) {
        super (TypeNameUse.TYPE_TOP_LEVEL, newTypeToken, null, givenPackage, givenName);
    }

    /** A name of a new type defined by the user. Not at the top (package) level, but as a part of another message.
     * @param newTypeToken See another public constructor that accepts a Token and other parameters.
     *  @param givenParent The immediate outer message. It must be non-null; otherwise use the other constructor.
     * */
    public TypeNameDefinition (Token newTypeToken, TypeNameDefinition givenParent, String givenName) {
        super (TypeNameUse.TYPE_LOWER_LEVEL, newTypeToken, givenParent, null, givenName);
    }

    /** A flexible constructor of both top-level and lower-level names for defining new types ("message", "enum"...).
     *  You can call it with givenPackage regardless of the level, and it ignores it for lower-level names.
     * @param givenPackage pass "packageName" variable from BNF grammar rules
     * */
    public TypeNameDefinition(Token newTypeToken, TypeNamePackage givenPackage, TypeNameDefinition givenParent, String givenName) {
        super (givenParent==null ? TypeNameUse.TYPE_TOP_LEVEL : TypeNameUse.TYPE_LOWER_LEVEL,
              newTypeToken,
              givenParent,
              givenParent==null ? givenPackage : null,
              givenName);
    }
}
