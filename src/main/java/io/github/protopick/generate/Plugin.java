package io.github.protopick.generate;

import io.github.protopick.compile.CompiledSet;
import io.github.protopick.compile.TypeDefinition;
import io.github.protopick.generate.Indented;
import io.github.protopick.parse.ParserContext;

/** Subject to change.
    A utility class to process the parsed .proto files and to invoke generating the export.
 *  Our parser collects types, fields... regardless of whether they're exported or not. Then here
 *  we collect what types, fields... we want to export.
 * */
public interface Plugin {
    /** Generate per type. If this calls compiledSet.generateOrReuse, then it's indirectly recursive. */
    public Indented generate (TypeDefinition typeDefinition, CompiledSet compiledSet);
    public Indented wrap( TypeDefinition typeDefinition, Indented generated );
}
