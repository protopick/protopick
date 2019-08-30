package io.github.protopick.generate;

import io.github.protopick.compile.CompiledSet;
import io.github.protopick.compile.Field;
import io.github.protopick.compile.TypeDefinition;
import io.github.protopick.generate.Indented;
import io.github.protopick.parse.ParserContext;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessagesMongo implements Plugin {
    private static Map<String, Object[]> primitiveTypes = new HashMap<>();
    private static void mapPrimitive( String primitive, Object... output ) {
        primitiveTypes.put( primitive, output );
    }
    static {
        mapPrimitive( "string", "bsonType: \"string\"" );
        mapPrimitive( "int32", "bsonType: \"int\"" );
        mapPrimitive( "int64", "bsonType: \"long\"" );
        // "uint32", "uint64" -> minimum: 0
        mapPrimitive( "bool", "type: \"boolean\""); // or: bsonType: "bool"
    }

    private Object[] generateSingle (Field field, CompiledSet compiledSet) {
        if (field.typeNameOfField.use.isPrimitive()) {
            return primitiveTypes.get(field.typeNameOfField.name);
        }
        else {
            if (field.isMap)
                throw new UnsupportedOperationException("Maps are not supported");
            TypeDefinition fieldType= field.typeNameOfField.resolve(compiledSet.context);
            if (fieldType==null) {
                throw new Error( "Field " +field+ " has unresolved type.");
            }
            return new Object[] { compiledSet.generateOrReuse(fieldType, this) };
        }
    }

    public Indented generate (TypeDefinition typeDefinition, CompiledSet compiledSet) {
        //System.out.println("Generate " +typeDefinition.typeNameDefinition.fullName());

        if (typeDefinition.isEnum) {
            System.out.println("--- enum");
            System.out.println( "Fields: " +typeDefinition.fields);
            Indented typeResult= new Indented();
            typeResult.add( "enum: [");
            boolean firstValue = true;
            for (Field value: typeDefinition.fields) {
                if (!firstValue)
                    typeResult.add( ", ");
                firstValue = false;
                typeResult.add( Tools.asJsonString(value.name) );
            }
            typeResult.add( "]");
            if (typeDefinition.getInstruction()!=null) {
                typeResult.add( ",\n" );
                typeResult.add( "description: ").add( Tools.asJsonString(typeDefinition.getInstruction().content) );
            }
            //System.out.println(typeResult);
            return typeResult;
        }
        else { // Message @TODO description of the message itself?
            Indented properties= new Indented();

            for (Field field: typeDefinition.fields) {
                final Indented property= new Indented();
                if (field.getInstruction()!=null)
                    property.add( "description: ", Tools.asJsonString(field.getInstruction().content), ",\n" );
                {
                    final Indented fieldLevel;
                    if (field.isRepeated) {
                        property.add( "\"type\": \"array\",\n");
                        property.add( "\"items\": {");
                        fieldLevel= new Indented();
                        property.add( fieldLevel );
                        property.add( "}");
                    } else {
                        fieldLevel = property;
                    }
                    fieldLevel.addArray( generateSingle(field, compiledSet) );
                }
                if (!properties.isEmpty())
                    properties.add(",\n");
                properties.add( field.name, ": {");
                properties.add( property );
                properties.add( "}" );
            }
            Indented typeResult= new Indented();
            typeResult.add( "bsonType: \"object\",", "\n", "properties: {" );
            typeResult.add( properties );
            typeResult.onNewLine().add( "}" );
            return typeResult; // @OTOD outside: prepend with typeDefinition.typeNameDefinition.name
        }
    }

    public Indented wrap( TypeDefinition typeDefinition, Indented generated ) {
        Indented out= new Indented();
        out.add( "db.createCollection(" +Tools.asJsonString(typeDefinition.typeNameDefinition.name)+ ", {" );
        out.add( new Indented(
                "\"capped\": false,\n",
                "\"validator\": {",
                new Indented(
                        "\"$jsonSchema\": {",
                        generated,
                        "}" ),
                "},\n",
                "\"validationLevel\": \"strict\",\n",
                "\"validationAction\": \"error\"" ));
        out.add( "} );" );
        return out;
    }
}