package protongo.compile;

import java.util.Properties;

/** A utility class to process the parsed .proto files and to invoke generating the export.
 *  Our parser collects types, fields... regardless of whether they're exported or not. Then here
 *  we collect what types, fields... we want to export.
 * */
public class Compile {
    String out; // Output directory, if specified. See option "o" in Run.java. May be null.
    Properties exportItems; // Export pair(s), if specified. See option "ep" in Run.java. Guaranteed not to be null, but it may be empty.
}
