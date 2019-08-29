package io.github.protopick.generate;

import io.github.protopick.compile.CompiledSet;
import io.github.protopick.compile.Field;
import io.github.protopick.compile.TypeDefinition;
import io.github.protopick.generate.Indented;
import io.github.protopick.parse.ParserContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class MessagesMongo implements Plugin {
    static String asJsonString(String orig) {
        return "\"" +orig.replace( "\\", "\\\\" ).replace( "\"", "\\\"" )+ "\"";
    }

    public Indented generate (TypeDefinition typeDefinition, CompiledSet compiledSet) {
        System.out.println("Generate " +typeDefinition.typeNameDefinition.fullName());
        Indented typeResult= new Indented();

        if (typeDefinition.isEnum) {
            System.out.println("--- enum");
            System.out.println( "Fields: " +typeDefinition.fields);
            typeResult.add( "enum: [");
            boolean firstValue = true;
            for (Field value: typeDefinition.fields) {
                if (!firstValue)
                    typeResult.add( ", ");
                firstValue = false;
                typeResult.add( asJsonString(value.name) );
            }
            typeResult.add( "]");
            if (typeDefinition.getInstruction()!=null) {
                typeResult.add( ",\n" );
                typeResult.add( "description: ").add( asJsonString(typeDefinition.getInstruction().content) );
            }
            //System.out.println(typeResult);
        }
        else { // Message
            typeResult.add( "bsonType: \"object\",", "\n", "properties: {" );
            Indented properties= new Indented();

            for (Field field: typeDefinition.fields) {
                if (!properties.isEmpty())
                    properties.add(",\n");
                properties.add( field.name, ": {");
                Indented property= new Indented();
                if (field.getInstruction()!=null)
                    property.add( "description: ").add( asJsonString(field.getInstruction().content) );
                properties.add( property );
                properties.add( "}" );
            }
            typeResult.add( properties );
            typeResult.onNewLine().add( "}" );
        }
        return typeResult; /*new Indented().add( typeDefinition.typeNameDefinition.name, ": {",
                                   typeResult,
                                   "}" );*/
    }
}