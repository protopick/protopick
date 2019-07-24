package protongo.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import protongo.compile.TypeName;
//import protongo.compile.TypeRelativeName;

/** Load parser(s) for included file(s).
 * This is not memory efficient. That's not the goal. */
public final class ProtoParserContext {
    public final List<String> importPaths= new ArrayList<>();

    private final Map<String, TypeName> newTypesMutable= new HashMap<>();

    /**"New types" mean type names used not for fields, but for types defined by the user ("message", "enum").
     * Full type name (including protobuf package, if any) -> Type. We add them in by addType(TypeName) as we parse. */
    public final Map<String, TypeName> newTypes= Collections.unmodifiableMap(newTypesMutable);

    public void addNewDefinedType( TypeName type ) {
        final String name = type.fullName();
        if (!type.use.definesNewType())
            throw new IllegalArgumentException("Don't add identifiers that define new fields: " +name+ ". Only add those that define new types.");
        if (newTypesMutable.containsKey(name))
            throw new IllegalArgumentException("Type with name " +name+ " has been registered already.");
        newTypesMutable.put(name, type);
    }

    // We parse each included file in a separate thread. That way waiting for files blocks as little as
    // possible. That includes the very first (start) file, even if it's just one. That's consistent.
    // Otherwise we'd have to clear ProtoParser.alreadyParsing for the starter thread (in case the
    // client starts another cycle from the same Thread).
    private final List<Thread> threads = new ArrayList<Thread>();

    /** Names of files that have been, or are being, processed. That prevents us from processing the
     * same file multiple times (if it's included from several files). That robust enough, because if the same file
     * is reachable through multiple paths (as in includePaths), that's incorrect (as per protoc 3.7.0). */
    private final Set<String> loadedFileNames= new HashSet<>();

    public synchronized void parse (String filePath) {
        if (!loadedFileNames.contains(filePath)) {
            loadedFileNames.add(filePath);

            Thread thread = new Thread(new Runnable() {
                public void run() {
                    // We must instantiate a new parser in a new thread
                    ProtoParser parser = new ProtoParser( loadFile(filePath) );
                    parser.registerWithContext(ProtoParserContext.this);
                    try {
                        parser.Input();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
            threads.add(thread);
        }
    }

    public synchronized void waitUntilComplete() {
        for( Thread thread: threads) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Including the path of the "root" file. This has to be a list, not a set, because `protoc`
     * applies the path folders in a given order. */
    public String includePaths[]= new String[0];

    private File resolveFile (String subPathAndName) {
        for (String path: includePaths) {
            File p= new File(path);
            if (!p.isDirectory())
                throw new IllegalArgumentException("Given path " +path+ " is not a directory.");
            File child= new File (p, subPathAndName);
            if (child.isFile())
                return child;
            if (child.isDirectory())
                throw new IllegalArgumentException("Given path " +path+ " and file " +subPathAndName+ " is not a file, but a directory.");
        }
        throw new IllegalArgumentException( "Given file " +subPathAndName+ " doesn't exist on the given path(s)." );
    }

    public InputStream loadFile (String subPathAndName) {
        try {
            return new FileInputStream(resolveFile(subPathAndName));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** Message name => HandlingInstruction */
    //public final Map<TypeName, HandlingInstruction> instructions= new HashMap<>();

    /*
    public final List<ProtoParser> parsers = new ArrayList<>();

    public void register (ProtoParser parser) {
        if (parsers.contains(parser)) {
            throw new IllegalStateException("Parser already registered.");
        }
        parsers.add (parser);
    }*/
}