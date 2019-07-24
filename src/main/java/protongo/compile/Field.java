package protongo.compile;

import protongo.parser.ParserConstants;
import protongo.parser.Token;

public class Field {
    public static enum Attribute { REQUIRED, OPTIONAL }
    public static Attribute attributeFrom(Token t) {
        switch (t.kind) {
            case ParserConstants.REQUIRED_TKN: return Attribute.REQUIRED;
            case ParserConstants.OPTIONAL_TKN: return Attribute.OPTIONAL;
            default: throw new IllegalArgumentException("Expecting 'required' or 'optional'.");
        }
    }

    public Attribute attribute;
    public boolean repeated;
    public TypeNameDefinition definitionType;
    public TypeNameOfField fieldType;
    public String name;
}
