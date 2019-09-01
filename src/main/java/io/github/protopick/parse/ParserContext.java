package io.github.protopick.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.github.protopick.compile.Field;
import io.github.protopick.compile.TypeDefinition;
import io.github.protopick.compile.TypeNameDefinition;
import io.github.protopick.compile.TypeNameOfField;

/** Load parser(s) for included file(s).
 * This is not memory efficient. That's not the goal. */
public final class ParserContext {
    // No need to synchronize, as it's set at the beginning only
    public final List<String> importPaths= new ArrayList<>();

    /**"New types" mean type names used not for fields, but for types defined by the user ("message", "enum").
     * Full type name (including protobuf package, if any) -> TypeDefinition. We add them in by addNewDefinedType(TypeName) as we parse.
     * <br/>Do SYNCHRONIZE any access by running within synchronized(newTypes) {...}.
     * */
    // We only keep a mutable map. Having a non-mutable view meant we'd need to synchronize on one of them only (or to synchronize on the whole ParserContext instance, which is not that granular).
    public final Map<String, TypeDefinition> newTypes = Collections.synchronizedMap( new LinkedHashMap<>() );

    /** Create a new TypeDefinition instance for the given name. Register it with the context, and return. */
    public TypeDefinition addNewDefinition( TypeNameDefinition typeName ) {
        final String fullName = typeName.fullName();
        synchronized (newTypes) {
            if (newTypes.containsKey(fullName))
                throw new IllegalArgumentException("Type with name " + fullName + " has been registered already.");
            TypeDefinition type= new TypeDefinition(typeName);
            newTypes.put(fullName, type);
            return type;
        }
    }

    // We parse each included file in a separate thread. That minimizes file I/O blocking.
    // That also applies to the very first (start) file, even if it's just one.
    // Otherwise we'd have to clear Parser.alreadyParsing for the starter thread (in case the
    // client starts another cycle from the same Thread).
    private final List<Thread> threads = Collections.synchronizedList( new ArrayList<Thread>() );

    /** Names of files that have been, or are being, processed. That prevents us from processing the
     * same file multiple times (if it's included from several files). That robust enough, because if the same file
     * is reachable through multiple paths (as in includePaths), that's incorrect (as per protoc 3.7.0). */
    private final Set<String> loadedFileNames= Collections.synchronizedSet( new HashSet<>() );

    public static final String ANY="Any", ANY_QUALIFIED="google.protobuf.Any";
    private static final String ANY_FILE="google/protobuf/any.proto";

    /** If the field is of type Any, or google.protobuf.Any, then validate that google/protobuf/any.proto was imported. */
    public void ifAnyValidateImport( TypeNameOfField typeNameOfField ) {
        if( (typeNameOfField.name.equals("Any") || typeNameOfField.name
                .equals("google.protobuf.Any"))
        && !newTypes.containsKey(ANY_QUALIFIED) ) {
            String parentOrContextOrPackageName= typeNameOfField.parentOrContextOrPackageName();
            throw new IllegalStateException(
                    "Must import " + ANY_FILE + " in order to use type " + typeNameOfField.name
                            +(parentOrContextOrPackageName!=null ? " in " +parentOrContextOrPackageName : "")+ ".");
        }
    }

    public void parse (String filePath) {
        synchronized (loadedFileNames) {
            if (!loadedFileNames.contains(filePath)) {
                loadedFileNames.add(filePath);

                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        if( filePath.equals(ANY_FILE) ) {
                            // Intentionally null, since it's not supposed to be used from here. This
                            // only indicates that type Any is available. However, its handling is done
                            // by TYPE_TKN token, and the plugin must handle like scalar types.
                            newTypes.put( ANY_QUALIFIED, null );
                        }
                        else {
                            // We must instantiate a new parser in a new thread
                            //System.out.println("Parser for " +filePath);
                            Parser parser = new Parser(loadFile(filePath));
                            parser.registerWithContext(ParserContext.this);
                            try {
                                parser.Input();
                                //System.out.println("-- parsed");
                            } catch (ParseException e) {
                                System.err.println(e);
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
                thread.start();
                threads.add(thread);
            }
        }
    }

    public void waitUntilComplete() {
        // DO NOT join all threads within synchronized (threads) {...}, because that would block any other
        // threads from importing any other .proto files (since parse(..) calls threads.add(..).
        // While we wait below, a thread could start another parser thread
        List<Thread> threadsSnapshot;
        while (true) {
            synchronized (threads) {
                if (threads.isEmpty())
                    return;
                threadsSnapshot = new ArrayList<>(threads);
                threads.removeAll( threadsSnapshot );
            }
            for (Thread thread : threadsSnapshot) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /** Including the path of the "root" file. This has to be an array, not a set, because `protoc`
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
}