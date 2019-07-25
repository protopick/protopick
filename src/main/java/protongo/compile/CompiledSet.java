package protongo.compile;

import java.util.Properties;

public class CompiledSet {
    String inputFileNames[];
    String out; // Output directory, if specified. See option "o" in Run.java. May be null.
    Properties exportItems; // Export pair(s), if specified. See option "ep" in Run.java. Guaranteed not to be null, but it may be empty.
}
