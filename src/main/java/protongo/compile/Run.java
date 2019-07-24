package protongo.compile;

import java.io.InputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
// Watch out: Both Apache Commons CLI, and JavaCC-generated code, define class "ParseException"
import org.apache.commons.cli.ParseException;
import protongo.parser.ProtoParser;
import protongo.parser.ProtoParserContext;

public class Run {
    public static void main(String... args) {
        final ProtoParserContext context= new ProtoParserContext();
        final String fileNames[];
        {
            final Options options = new Options();
            // Option -I and --proto_path is based on `protoc`. You can repeat it, passing a different value each
            // time. It's optional. However, if you do import any files from the same folder as the 'start' file (on
            // which you're invoking this), you must pass '-IPATH' or `--proto_path` for that 'start' folder, too.
            Option proto_path= Option
                    .builder("I")
                    .longOpt( "proto_path" )
                    .desc( "Import path. You can pass multiple, one '-I path' or '--proto_path path' per each." )
                    .hasArg() // Don't use .hasArgs(), because that consumes the rest of args, including the filenames to parse!
                    .required().build();
            proto_path.setRequired (true);
            options.addOption (proto_path);
            options.addOption("h", "help", false, "Show this help.");

            CommandLineParser parser = new DefaultParser();
            try {
                CommandLine cli = parser.parse(options, args);

                fileNames= cli.getArgs();
                if (fileNames.length==0 || cli.hasOption('h')) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "gradle run --args='[OPTIONS] PROTO_FILES'", options );
                    return;
                }

                // Don't use proto_path.getValue(), it was null!
                context.includePaths= cli.getOptionValues('I');
            } catch (ParseException exp) {
                System.err.println("Error parsing the parameters: " + exp.getMessage());
                return;
            }
        }
        for (String fileName: fileNames) {
            context.parse( fileName );
            }
        context.waitUntilComplete();
    }
}