package io.github.protopick.generate;

import io.github.protopick.compile.CompiledSet;
import io.github.protopick.compile.TypeDefinition;
import io.github.protopick.parse.ParserContext;
import java.util.Map;

public class MessagesMongo implements Plugin {
    public void generate (ParserContext context, CompiledSet compiledSet) {
        for (Map.Entry<String, TypeDefinition> entry: context.newTypes.entrySet()) {

        }

    }
}