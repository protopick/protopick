package io.github.protopick.generate;

import io.github.protopick.compile.CompiledSet;
import io.github.protopick.compile.Field;
import io.github.protopick.compile.TypeDefinition;
import java.util.HashMap;
import java.util.Map;

public class MessagesMongo implements Plugin {
    private static Map<String, Object[]> primitiveTypes = new HashMap<>();
    private static void mapPrimitive( String primitive, Object... output ) {
        if( primitiveTypes.containsKey(primitive) )
            throw new IllegalArgumentException("Already set.");
        primitiveTypes.put( primitive, output );
    }
    // https://developers.google.com/protocol-buffers/docs/proto3#scalar
    // https://docs.mongodb.com/manual/core/schema-validation/
    // https://docs.mongodb.com/manual/reference/bson-types/
    // https://docs.mongodb.com/manual/reference/operator/query/jsonSchema/#jsonschema-keywords
    // https://json-schema.org/understanding-json-schema/reference/array.html
    static {
        mapPrimitive( "double", "bsonType: \"double\"" );

        // https://json-schema.org/understanding-json-schema/reference/numeric.html#number says that
        // JSON type number is analogous to Python float. And Protobuf float translates to Python float:
        // https://developers.google.com/protocol-buffers/docs/proto3#scalar, hence using JSON number:
        mapPrimitive( "float", "type: \"number\"" );

        mapPrimitive( "int32", "bsonType: \"int\"" );
        mapPrimitive( "uint32", "bsonType: \"int\"", ",", "\"minimum\": 0" );
        mapPrimitive( "sint32", "bsonType: \"int\"" );
        mapPrimitive( "int64", "bsonType: \"long\"" );
        mapPrimitive( "uint64", "bsonType: \"long\"", ",", "\"minimum\": 0" );
        mapPrimitive( "sint64", "bsonType: \"long\"" );

        mapPrimitive( "fixed32", "bsonType: \"int\"" );
        mapPrimitive( "sfixed32", "bsonType: \"int\"" );
        mapPrimitive( "fixed64", "bsonType: \"long\"" );
        mapPrimitive( "sfixed64", "bsonType: \"long\"" );

        mapPrimitive( "bool", "type: \"boolean\""); // or: bsonType: "bool"
        mapPrimitive( "string", "bsonType: \"string\"" );

        // https://stackoverflow.com/questions/45106141/error-while-reading-blob-binary-data-from-mongodb-using-java
        // https://mongodb.github.io/mongo-java-driver/3.7/javadoc/org/bson/types/Binary.html - converts to/from byte[]
        // https://docs.mongodb.com/manual/reference/limits/#BSON-Document-Size - max 16MB
        // Protoc translates "bytes" to com.google.protobuf.ByteString, which easily converts to/from byte[]:
        // https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/ByteString#toByteArray--
        // https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/ByteString#copyFrom-byte:A-
        mapPrimitive( "bytes", "bsonType: \"binData\"" );
    }

    private Object[] generateSingle (Field field, CompiledSet compiledSet) {
        if (field.typeNameOfField.use.isPrimitive()) {
            return primitiveTypes.get(field.typeNameOfField.name);
        }
        else {
            if (field.isMap) {
                // https://stackoverflow.com/questions/17877619/json-schema-with-dynamic-key-field-in-mongodb
                throw new UnsupportedOperationException("Maps are not supported");
            }
            else {
                TypeDefinition fieldType = field.typeNameOfField.resolve(compiledSet.context);
                if (fieldType == null) {
                    throw new Error("Field " + field + " has unresolved type.");
                }
                return new Object[]{compiledSet.generateOrReuse(fieldType, this)};
            }
        }
    }

    public Indented generate (TypeDefinition typeDefinition, CompiledSet compiledSet) {
        //System.out.println("Generate " +typeDefinition.typeNameDefinition.fullName());

        if (typeDefinition.isEnum) {
            Indented typeResult= new Indented();
            typeResult.add( "enum: [");
            boolean firstValue = true;
            for (Field value: typeDefinition.fields) {
                if (!firstValue)
                    typeResult.add( ", ");
                firstValue = false;
                typeResult.add( Tools.asStringLiteral(value.name) );
            }
            typeResult.add( "]");
            if (typeDefinition.getInstruction()!=null) {
                typeResult.add( ",\n" );
                typeResult.add( "description: ").add( Tools.asStringLiteral(typeDefinition.getInstruction().content) );
            }
            //System.out.println(typeResult);
            return typeResult;
        }
        else { // Message @TODO description of the message itself?
            Indented properties= new Indented();

            for (Field field: typeDefinition.fields) {
                final Indented property= new Indented();
                if (field.getInstruction()!=null)
                    property.add("description: ", Tools.asStringLiteral(field.getInstruction().content), ",\n" );
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
        out.add( "db.createCollection(" +Tools.asStringLiteral(typeDefinition.typeNameDefinition.name)+ ", {" );
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