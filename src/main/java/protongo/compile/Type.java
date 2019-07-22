package protongo.parser;

/** This represents a Message, sub-sub...Message, an Enum... */
public class Type implements Comparable<Type> {
    /*  Empty string is replaced with null. */
    private final String packageName;
    private final String name;
    private final Type parent;

    private Type (Type givenParent, String givenPackage, String givenName) {
        parent = givenParent;
        packageName = givenPackage!=null && !givenPackage.isEmpty()
                ? givenPackage
                : null;
        if (givenName==null || givenName.isEmpty())
            throw new IllegalArgumentException("Type name must not be empty.");
        name = givenName;
    }

    /** @param givenPackage If null or an empty string, then it's no package/default package. */
    Type (String givenPackage, String givenName) {
        this (null, givenPackage, givenName);
    }

    Type (Type givenParent, String givenName) {
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

    public int compareTo(Type other) {
        return fullName().compareTo( other.fullName() );
    }
    public boolean equals(Type other) {
        return other!=null && fullName().equals( other.fullName() );
    }
    public int hashCode() {
        return fullName().hashCode();
    }
}
