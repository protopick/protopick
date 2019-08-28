package io.github.protopick.compile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import io.github.protopick.generate.Indented;
import io.github.protopick.generate.Plugin;
import io.github.protopick.parse.ParserContext;

public final class CompiledSet {
    public final ParserContext context;

    public String inputFileNames[];
    public String out; // Output directory, if specified. If the specified directory didn't contain a trailing slash (or backslash on Windows), it's added. An empty string if not specified (then use the current folder).
    public Properties exportItems; // Export pair(s), if specified. See option "ep" in Run.java. Guaranteed not to be null, but it may be empty.
    // @TODO Do Properties preserve the order? IF not, use a LinkedHasHMap

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
                final Indented generated= generateOrReuse(typeDefinition, null/*@TODO plugin*/);
                throw new Error("@TODO");
            }
            else
                throw new IllegalArgumentException("Export item " +itemName+ " not found in the source.");
        }
    }

    /** Already compiled items with generated output. */
    final Map<TypeDefinition, Indented> generated = new LinkedHashMap<>();

    void generateAll(Plugin plugin) {
        // @TODO use this.exportItems instead
        for (Map.Entry<String, TypeDefinition> entry: context.newTypes.entrySet()) {
            generateOrReuse (entry.getValue(), plugin);
        }
    }

    public Indented generateOrReuse (TypeDefinition typeDefinition, Plugin plugin) {
        final Indented existing = generated.get (typeDefinition);
        if (existing!=null)
            return existing;
        final Indented generatedNow= plugin.generate (typeDefinition, this);
        generated.put (typeDefinition, generatedNow);
        return generatedNow;
    }
}
