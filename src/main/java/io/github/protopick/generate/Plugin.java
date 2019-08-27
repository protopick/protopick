package io.github.protopick.generate;


/** Subject to change.
    A utility class to process the parsed .proto files and to invoke generating the export.
 *  Our parser collects types, fields... regardless of whether they're exported or not. Then here
 *  we collect what types, fields... we want to export.
 * */
public interface Plugin {
    //public void generate (ParserContext context, CompiledSet compiledSet);
}
