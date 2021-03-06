package io.github.protopick.generate;

import io.github.protopick.compile.CompiledSet;
import io.github.protopick.compile.Field;
import io.github.protopick.compile.TypeDefinition;
import io.github.protopick.compile.TypeNameOfField;
import io.github.protopick.parse.ParserContext;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
        mapPrimitive( "double", "\"bsonType\": \"double\"" );

        // https://json-schema.org/understanding-json-schema/reference/numeric.html#number says that
        // JSON type number is analogous to Python float. And Protobuf float translates to Python float:
        // https://developers.google.com/protocol-buffers/docs/proto3#scalar, hence using JSON number:
        mapPrimitive( "float", "\"type\": \"number\"" );

        mapPrimitive( "int32", "\"bsonType\": \"int\"" );
        mapPrimitive( "uint32", "\"bsonType\": \"int\"", ",", "\n\"minimum\": 0" );
        mapPrimitive( "sint32", "\"bsonType\": \"int\"" );
        mapPrimitive( "int64", "\"bsonType\": \"long\"" );
        mapPrimitive( "uint64", "\"bsonType\": \"long\"", ",", "\n\"minimum\": 0" );
        mapPrimitive( "sint64", "\"bsonType\": \"long\"" );

        mapPrimitive( "fixed32", "\"bsonType\": \"int\"" );
        mapPrimitive( "sfixed32", "\"bsonType\": \"int\"" );
        mapPrimitive( "fixed64", "\"bsonType\": \"long\"" );
        mapPrimitive( "sfixed64", "\"bsonType\": \"long\"" );

        mapPrimitive( "bool", "\"type\": \"boolean\""); // or: bsonType: "bool"
        mapPrimitive( "string", "\"bsonType\": \"string\"" );

        // https://stackoverflow.com/questions/45106141/error-while-reading-blob-binary-data-from-mongodb-using-java
        // https://mongodb.github.io/mongo-java-driver/3.7/javadoc/org/bson/types/Binary.html - converts to/from byte[]
        // https://docs.mongodb.com/manual/reference/limits/#BSON-Document-Size - max 16MB
        // Protoc translates "bytes" to com.google.protobuf.ByteString, which easily converts to/from byte[]:
        // https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/ByteString#toByteArray--
        // https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/ByteString#copyFrom-byte:A-
        mapPrimitive( "bytes", "\"bsonType\": \"binData\"" );
        mapPrimitive( ParserContext.ANY, "\"type\": \"object\"" );
        mapPrimitive( ParserContext.ANY_QUALIFIED, "\"type\": \"object\"" );
    }

    private Object[] singleTypeEntries( TypeNameOfField typeNameOfField, CompiledSet compiledSet,
            boolean ifPrimitiveAddIndentation ) {
        Object keyEntries[];
        if( typeNameOfField.use.isPrimitive() ) {
            compiledSet.context.ifAnyValidateImport( typeNameOfField );

            keyEntries= primitiveTypes.get( typeNameOfField.name );
            if( ifPrimitiveAddIndentation ) {
                keyEntries= new Object[] { new Indented().addArray(keyEntries) };
            }
        }
        else {
            TypeDefinition fieldOrKeyType = typeNameOfField.resolve(compiledSet.context);
            keyEntries= new Object[]{ compiledSet.generateOrReuse(fieldOrKeyType, this) };
        }
        return keyEntries;
    }

    private Object[] generateSingle (Field field, CompiledSet compiledSet) {
        final Object keyEntries[]= singleTypeEntries( field.typeNameOfField, compiledSet, field.isMap );
        if( field.typeNameOfField.use.isPrimitive() && !field.isMap ) {
            return keyEntries;
        }
        else {
            if (field.isMap) {
                // https://stackoverflow.com/questions/17877619/json-schema-with-dynamic-key-field-in-mongodb
                final List<Object> result= new ArrayList<>();
                result.add( "\"type\": \"array\"" );
                result.add( "\"items\": {" );
                    Indented pair= new Indented();
                    pair.add( "\"bsonType\": \"object\",\n" );
                    pair.add( "\"additionalProperties\": false,\n" );
                    pair.add( "\"key\": {" );
                        pair.addArray( keyEntries );
                    pair.add( "},\n" );
                    pair.add( "\"value\": {" );
                        pair.addArray( singleTypeEntries(field.typeNameOfMapValues, compiledSet, true) );
                    pair.add( "}\n" );
                    result.add( pair );
                result.add( "}");
                return result.toArray();
            }
            else {
                return keyEntries;
            }
        }
    }

    @Override public Indented generate (TypeDefinition typeDefinition, CompiledSet compiledSet) {
        //System.out.println("Generate " +typeDefinition.typeNameDefinition.fullName());

        if (typeDefinition.isEnum) {
            // Do NOT add "description" based on typeDefinition.getInstruction().content. Why? Because the
            // field currently being defined already had "description" generated in the grandparent function call.
            Indented typeResult= Indented.newSameIndent();
            typeResult.add( "\"enum\": [");
            boolean firstValue = true;
            for (Field value: typeDefinition.fields) {
                if (!firstValue)
                    typeResult.add( ", ");
                firstValue = false;
                typeResult.add( Tools.asStringLiteral(value.name) );
            }
            typeResult.add( "]");
            //System.out.println(typeResult);
            return typeResult;
        }
        else { // Message @TODO description of the message itself?
            Indented properties= new Indented();
            properties.add( new FirstPerGroup( "_id",
                                               "\"_id\": {\"bsonType\": \"objectId\"}"
                                               + (typeDefinition.fields.isEmpty() ? "" : ",\n")) );

            boolean hadPreviousProperties= false;
            for (Field field: typeDefinition.fields) {
                final Indented property= new Indented();
                if (field.getInstruction()!=null) {//@TODO consider "title" instead
                    property.add("\"description\": ", Tools.asStringLiteral(field.getInstruction().content), ",");
                    property.onNewLine(); //property.add( "\n" );
                }
                {
                    final Indented fieldLevel;
                    if (field.isRepeated) {
                        property.add( "\"type\": \"array\",\n" );
                        fieldLevel= new Indented();
                        property.add( "\"items\": {" );
                            property.add( fieldLevel );
                        property.add( "}");
                    } else {
                        fieldLevel = property;
                    }
                    fieldLevel.addArray( generateSingle(field, compiledSet) );
                }
                if( hadPreviousProperties )
                    properties.add(",\n");
                hadPreviousProperties= true;
                properties.add( Tools.asStringLiteral(field.name), ": {");
                    properties.add( property );
                properties.add( "}" );
            }

            Indented typeResult= new Indented();
            // https://docs.mongodb.com/manual/reference/operator/query/jsonSchema/#jsonschema-keywords > additionalProperties
            // https://stackoverflow.com/questions/48491556/mongodb-jsonschema-validation-additionalproperties
            typeResult.add( "\"bsonType\": \"object\",\n" );
            typeResult.add( "\"additionalProperties\": false,\n" );
            typeResult.add( "\"properties\": {" );
            typeResult.add( properties );
            typeResult.onNewLine().add( "}" );
            return typeResult;
        }
    }

    public Indented wrap( TypeDefinition typeDefinition, Indented generated ) {
        Indented out= new Indented();
        out.add( "db.createCollection(" +Tools.asStringLiteral(typeDefinition.typeNameDefinition.name)+ ", {" );
        out.add( new Indented(
                "\"capped\": false,\n", //@TODO false is default -do we need this?
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