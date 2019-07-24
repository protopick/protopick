package protongo.compile;

public class Field {
    public static enum Attribute { REQUIRED, OPTIONAL }
    public Attribute attribute;
    public boolean repeated;
    public TypeNameOfField type;
    public String name;
}
