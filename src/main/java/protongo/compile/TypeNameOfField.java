package protongo.compile;

import protongo.parser.ParserConstants;
import protongo.parser.Token;

/** Indicate a type that a field definition refers to. Not for defining new types/enums. */
public final class TypeNameOfField extends TypeName {
    /** For recording references to user-defined types. They may be simple strings, or dot-separated:
     * package-qualified or "relative" within the current package.
     * As to which one it is, we don't know - until we call resolve().
     * @param givenPackage pass "packageName" variable from BNF grammar rules
     * @param givenName May contain dot-separated parts, or just one name.
     * */
    public TypeNameOfField (TypeNamePackage givenPackage, TypeNameDefinition givenContext, String givenName) {
        super (TypeNameUse.FIELD, null, givenContext, givenPackage, givenName);
    }

    /** Use when defining a new field whose type is a simple/primitive (built-in) Protobuf type.
     * @param simpleType Token of the built-in Protobuf type. NOT the same meaning as newTypeToken in the other constructors. */
    public TypeNameOfField (Token simpleType) {
        super (TypeNameUse.FIELD_PRIMITIVE, null, null, null, simpleType.image);
        if (simpleType.kind!=ParserConstants.TYPE_TKN)
            throw new IllegalArgumentException("Must pass a built-in type, not: " +simpleType);
    }

    /* Situation: We're parsing a message. One of its fields refers to another type (a message, enum...).
       That type reference is either:
       1. Qualified (with a dot or several) type with a package name: name.of.a.package.nameOfType.[nameOfSubtype...]
       2. Qualified (with a dot or several, or none) sub-message: nameOfAnotherMessage[.nameOfItsInnerMessage...],
       where nameOfAnotherMessage is a direct child of any ancestor of the current message (immediate parent of the field being defined),
       up to and including the top level (the current package) as the farthest such ancestor.
       That way this includes any matching types from the top level of the current package, too.
       If there's no current package, #2 assumes it to be an empty string.
       <br/>
       #1 has precedence over #2, because if there's such a conflict, #2 can work around by providing
       a path from one level higher. If it reaches the package level, and if there's still a conflict,
       then the user can work around by adding a package prefix to #2 (effectively making it another #1).
       <br/>
       While parsing, even if that other type is in the same file, we don't want to start parsing it right now.
       We've either already parsed it, or we will parse it later, in this same Parser instance.
       Hence, BNF rules don't resolve, they only register TypeNameOfField instances with ParserContext. We call this resolveFieldType() only later, once all BNF rules were processed.
       (Also, the same package may be across multiple files => involving multiple parsers...)
       <br/>
       Do NOT use until the whole input gets parsed. Otherwise this can't identify "relative" (sub-message) names.
    */
    public TypeNameDefinition resolve (protongo.parser.ParserContext context) {
        if (!use.mayBeRelative())
            throw new UnsupportedOperationException("You can't call resolve() for identifiers that define new types. Only call it for identifiers that define non-simple fields.");
        // @TODO prepopulate: com.google.Empty etc.
        // 1. Try this,name as a combination of a package name and a (potentially multi-level) type name within that package.
        if (context.newTypeNames.containsKey(name))
            return context.newTypeNames.get(name);
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
            if (context.newTypeNames.containsKey(candidate))
                return context.newTypeNames.get(candidate);
            int i= subContext.lastIndexOf(".");
            if (i>0) {
                subContext= subContext.substring(0, i);
            }
            else
                return null;
        }
    }
}
