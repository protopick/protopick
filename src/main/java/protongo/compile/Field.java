package protongo.compile;

import protongo.parser.ParserConstants;
// We only use Token as a parameter, but we don't store it. Otherwise it would hold on all the previous tokens, too.
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
    public boolean isRepeated;
    public boolean isMap;

    /** This is redundant but worthwhile. The client code usually has TypeDefinition instance already. However, having it here means
    that we don't have to pass a definition/context type in Parser.jjt as much, hence simpler. */
    public final TypeDefinition typeDefinition;

    /** For non-map fields, this is the type of the field (its value). For map fields, this is the type of the keys. */
    public TypeNameOfField typeNameOfField;
    /** Used only if .isMap is true. For key type use typeNameOfField. */
    public TypeNameOfField typeNameOfMapValues;

    public String name;

    /** Link the new field and givenTypeDefinition both ways: 1. set this.typeDefinition,
     * 2. add this field to givenTypeDefinition.fields. */
    public Field (TypeDefinition givenTypeDefinition) {
        typeDefinition= givenTypeDefinition;
        typeDefinition.fields.add (this);
    }
}
