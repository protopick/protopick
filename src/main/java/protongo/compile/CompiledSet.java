package protongo.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import protongo.generate.Indent;
import protongo.parser.ParserContext;

public final class CompiledSet {
    final ParserContext context;

    public String inputFileNames[];
    public String out; // Output directory, if specified. If the specified directory didn't contain a trailing slash (or backslash on Windows), it's added. An empty string if not specified (then use the current folder).
    public Properties exportItems; // Export pair(s), if specified. See option "ep" in Run.java. Guaranteed not to be null, but it may be empty.

    /* package-visible only */
    CompiledSet(ParserContext givenContext) {
        context= givenContext;
    }

    void compile () {//@TODO listExportItems
        if (!exportItems.isEmpty()) {
            for (String itemName : exportItems.stringPropertyNames()) {
                // Prefix with the output path. (.stringPropertyNames() is a snapshot, so we can change the properties object within this loop)
                exportItems.setProperty( itemName, out + exportItems.getProperty(itemName) );
            }
        }
        else {
            for( Map.Entry<String, TypeDefinition> n: context.newTypes.entrySet() ) {
            }
            throw new Error("Take them all -TODO");
        }
        for (final String itemName : exportItems.stringPropertyNames()) {
            final String outputFilePath = exportItems.getProperty(itemName);
            System.out.println( "context.newTypes: " +context.newTypes.keySet());

            if (context.newTypes.containsKey(itemName)) {
                final TypeDefinition typeDefinition= context.newTypes.get(itemName);
                final Indent generated= generateOrReuse(typeDefinition);
                throw new Error("@TODO");
            }
            else
                throw new IllegalArgumentException("Export item " +itemName+ " not found in the source.");
        }
    }

    /** Already compiled items with generated output. */
    private final Map<TypeNameDefinition, Indent> generated = new HashMap<>();

    Indent generateOrReuse(TypeDefinition typeDefinition) {
        if (!generated.containsKey(typeDefinition.typeNameDefinition))
            generated.put (typeDefinition.typeNameDefinition, generate(typeDefinition));
        return generated.get(typeDefinition.typeNameDefinition);
    }

    Indent generate(TypeDefinition typeDefinition) {
        throw new Error("@TODO");
    }
}
