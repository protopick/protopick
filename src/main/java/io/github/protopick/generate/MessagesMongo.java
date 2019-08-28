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
    public Indented generate (TypeDefinition typeDefinition, CompiledSet compiledSet) {
        Indented innerResult= new Indented();
        System.out.println("Generate " +typeDefinition.typeNameDefinition.fullName());
        if (typeDefinition.isEnum) {
            System.out.println("--- enum");
            System.out.println( "Fields: " +typeDefinition.fields);
            innerResult.add( "enum: [");
            boolean firstValue = true;
            for (Field value: typeDefinition.fields) {
                if (!firstValue)
                    innerResult.add( ", ");
                firstValue = false;
                innerResult.add( "\"" ).add( value.name ).add( "\"" );
            }
            innerResult.add( "]");
            System.out.println(innerResult);
        }
        else { // Message

        }
        return new Indented().add( typeDefinition.typeNameDefinition.name, ": {\n",
                                   innerResult,
                                   "\n}" );
    }
}