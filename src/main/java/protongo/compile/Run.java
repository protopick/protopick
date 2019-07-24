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
            // Even though proto_path (and some other fields) are required, we don't call .required()
            // here. Otherwise id the user doesn't pass any parameters, parsing fails and we can't show
            // them a help screen. Hence we check the presence of required parameters manually later.
            Option proto_path= Option
                    .builder("I")
                    .longOpt( "proto_path" )
                    .desc( "Import path. You can pass multiple, one '-I path' or '--proto_path path' per each." )
                    .hasArg() // Don't use .hasArgs(), because that consumes the rest of args, including the filenames to parse!
                    .build();
            options.addOption (proto_path);

            Option exports= Option
                    .builder("ep")
                    .longOpt( "export_pair")
                    .numberOfArgs(2)
                    .valueSeparator()
                    .desc( "Export pair: package.qualified.type=output/file-with-extension. You can pass multiple, "
                           +"one '-ep pair' or '--export_pair pair' per each type to be exported.")
                    .build();
            options.addOption(exports);

            // Later: '-ei item' or '--export_item item'
            // together with '-ee extension' or '--export_extension extension' and
            // '-en sand-wich', '-en under_score' or '-en lowercase' (or --export_naming with the same values)
            // When exporting, the output filenames are based on Protoc 'package'. They're not based on the location of the .proto files.
            Option output= Option
                    .builder("o")
                    .longOpt("out")
                    .desc( "Output folder. If not present, using the current directory. This is prefixed "
                           +"in front of each export subpath from 'e' or '--export' parameter(s).")
                    .hasArg()
                    .build();
            options.addOption(output);

            options.addOption("h", "help", false, "Show this help.");

            CommandLineParser parser = new DefaultParser();
            Compile compile= new Compile();
            try {
                CommandLine cli = parser.parse(options, args);

                fileNames= cli.getArgs();
                if (fileNames.length==0 || cli.hasOption('h')) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "gradle run --args='arguments-as-per-below' OR: java protongo.compile.Run arguments-as-per-below", options );
                    return;
                }

                // Don't use proto_path.getValue(), it was null!
                context.includePaths= cli.getOptionValues('I');
                //System.out.println( "context.includePaths: " +java.util.Arrays.asList(context.includePaths));
                if (context.includePaths==null) // if the option is not present, this not an empty array, but null!
                    throw new IllegalArgumentException("Must pass some -I or --proto_path, even for the folder(s) where the start file(s) are.");

                compile.out= cli.getOptionValue('o');
                compile.exportItems= cli.getOptionProperties("ep"); // Contrary to cli.getOptionValues(String), this is guaranteed non-null
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