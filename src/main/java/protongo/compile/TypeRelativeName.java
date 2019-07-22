package protongo.compile;

public final class TypeRelativeName implements Comparable<TypeRelativeName> {
    private String name;
    public TypeRelativeName(String givenName) { name= givenName; }

    @Override
    public int compareTo(TypeRelativeName other) { return name.compareTo(other.name); }

    @Override
    public boolean equals(Object other) {
        return other instanceof TypeRelativeName && name.equals( ((TypeRelativeName)other).name );
    }
    public int hashCode() { return name.hashCode(); }
    public String toString() { return name; }
}
