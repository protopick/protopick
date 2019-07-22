package protongo.compile;

import protongo.parser.ProtoParserConstants;
import protongo.parser.Token;

/** This represents a Message, sub-sub...Message, an Enum... */
public class TypeFullName implements Comparable<TypeFullName> {
    /*  Empty string is replaced with null. */
    private final String packageName;
    private final String name;
    private final TypeFullName parent;
    // private final int tokenKind;

    /* @param token Indicates what kind of 'type' (e.g. "message", "enum"...) this identifier is for. It may be null
    * for usages that we're not intereste in (for example other usages of ClassPart BNF rule). */
    private TypeFullName (/*Token token,*/TypeFullName givenParent, String givenPackage, String givenName) {
        parent = givenParent;
        packageName = givenPackage!=null && !givenPackage.isEmpty()
                ? givenPackage
                : null;
        if (givenName==null || givenName.isEmpty())
            throw new IllegalArgumentException("Type name must not be empty.");
        name = givenName;
        // if (parent==null && token.kind!=ProtoParserConstants.MESSAGE_TKN ) throw new IllegalArgumentException("Top-level type must be a message, not a " +token.image);
    }

    /** @param givenPackage If null or an empty string, then it's no package/default package. */
    public TypeFullName (String givenPackage, String givenName) {
        this (null, givenPackage, givenName);
    }

    public TypeFullName (TypeFullName givenParent, String givenName) {
        this (givenParent, givenParent.packageName, givenName);
    }

    public String fullName() {
        if (parent != null)
            return parent.fullName() + '.' +name;
        else if (packageName!=null)
            return packageName + '.' +name;
        else
            return name;
    }

    public int compareTo(TypeFullName other) {
        return fullName().compareTo( other.fullName() );
    }
    @Override
    public boolean equals(Object other) {
        return other!=null && fullName().equals( ((TypeFullName)other).fullName() );
    }
    public int hashCode() {
        return fullName().hashCode();
    }
}
