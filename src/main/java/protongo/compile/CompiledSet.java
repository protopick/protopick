package protongo.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import protongo.generate.Indent;
import protongo.parser.ParserContext;

public final class CompiledSet {
    public String inputFileNames[];
    public String out; // Output directory, if specified. If the specified directory didn't contain a trailing slash (or backslash on Windows), it's added. An empty string if not specified (then use the current folder).
    public Properties exportItems; // Export pair(s), if specified. See option "ep" in Run.java. Guaranteed not to be null, but it may be empty.

    CompiledSet() {} // package-visible only

    /** Already compiled items with generated output. Only put to this map once an item's generated
     * output is complete. */
    private final Map<TypeNameDefinition, Indent> generated = new HashMap<>();

    void compile (ParserContext context) {
        if (!exportItems.isEmpty()) {
            for (String itemName : exportItems.stringPropertyNames()) {
                // .stringPropertyNames() is a snapshot, so we can change the properties object within this loop
                exportItems.setProperty( itemName, out + exportItems.getProperty(itemName) );
            }
        }
        else {
            for( Map.Entry<String, TypeDefinition> n: context.newTypes.entrySet() ) {

            }
            throw new Error("Take them all -TODO");
        }
        for (String itemName : exportItems.stringPropertyNames()) {
            String outputFilePath = exportItems.getProperty(itemName);
            System.out.println( "context.newTypes: " +context.newTypes.keySet());
            if (context.newTypes.containsKey(itemName)) {
                TypeDefinition typeDefinition= context.newTypes.get(itemName);
                if (!generated.containsKey(typeDefinition.typeNameDefinition))
                    generated.put (typeDefinition.typeNameDefinition, collect(typeDefinition.typeNameDefinition, context));
            }
            else
                throw new IllegalArgumentException("Export item " +itemName+ " not found in the source.");
        }
    }

    Indent collect(TypeNameDefinition typeNameDefinition, ParserContext context) {
        return null;
    }
}
