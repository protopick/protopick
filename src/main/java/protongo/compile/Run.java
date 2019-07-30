package protongo.compile;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
// Watch out: Both Apache Commons CLI, and JavaCC-generated code, define class "ParseException"
import org.apache.commons.cli.ParseException;
import protongo.generate.Generator;

public class Run {
    public static void main(String... args) {
        final protongo.parser.ParserContext context= new protongo.parser.ParserContext();
        final CompiledSet compiledSet= new CompiledSet();
        final Generator generator;
        {
            final Options options = new Options();
            // Option -I and --proto_path is based on `protoc`. You can repeat it, passing a different value each
            // time. It's optional. However, if you do import any files from the same folder as the 'start' file (on
            // which you're invoking this), you must pass '-IPATH' or `--proto_path` for that 'start' folder, too.
            // Even though proto_path (and some other fields) are required, we don't call .required()
            // here. Otherwise id the user doesn't pass any parameters, parsing fails and we can't show
            // them a help screen. Hence we check the presence of required parameters manually later.
            Option protoPath= Option
                    .builder("I")
                    .longOpt( "proto_path" )
                    .desc( "Import path. You can pass multiple, one '-I path' or '--proto_path path' per each." )
                    .hasArg() // Don't use .hasArgs(), because that consumes the rest of args, including the filenames to parse!
                    .build();
            options.addOption (protoPath);

            Option generate= Option
                    .builder("g")
                    .longOpt("generate")
                    .desc( "Generator class. A full, package-qualified name.")
                    .hasArg()
                    .build();
            options.addOption(generate);

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
            Option output= Option.builder("o")
                    .longOpt("out")
                    .desc( "Output folder. If not present, using the current directory. This is prefixed "
                           +"in front of each export subpath from 'e' or '--export' parameter(s).")
                    .hasArg().build();
            options.addOption(output);
            /* @TODO
            Option instructedOnly= Option
                    .builder("io")
                    .longOpt("instructed_only")
                    .desc("Whether to generate only for entries that have a 'handling instruction'.")
                    .build();*/

            options.addOption("h", "help", false, "Show this help.");

            CommandLineParser parser = new DefaultParser();
            try {
                CommandLine cli = parser.parse(options, args);

                compiledSet.inputFileNames= cli.getArgs();
                if (compiledSet.inputFileNames.length==0 || cli.hasOption('h')) {
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp( "gradle run --args='-I path -I another/path options-as-per-below file.proto another-file.proto' OR: java protongo.compile.Run the-same-arguments", options );
                    return;
                }

                // Don't use proto_path.getValue(), it was null!
                context.includePaths= cli.getOptionValues('I');
                //System.out.println( "context.includePaths: " +java.util.Arrays.asList(context.includePaths));
                if (context.includePaths==null) // if the option is not present, this not an empty array, but null!
                    throw new IllegalArgumentException("Must pass some -I or --proto_path, even for the folder(s) where the start file(s) are.");

                String generatorClassName=cli.getOptionValue('g');
                if (generatorClassName==null)
                    throw new IllegalArgumentException("Must pass an -g or --generator option with a generator's full class name.");
                try {
                    Class<Generator> generatorClass = (Class<Generator>) Class.forName(generatorClassName);
                    generator= generatorClass.newInstance();
                }
                catch(ClassNotFoundException|InstantiationException|IllegalAccessException e) {
                    throw new RuntimeException("Couldn't load a generator class " +generatorClassName, e);
                }

                compiledSet.out= cli.getOptionValue('o');
                if (compiledSet.out == null)
                    compiledSet.out = "";
                else if (!compiledSet.out.endsWith(File.separator))
                    compiledSet.out += java.io.File.separatorChar;

                compiledSet.exportItems= cli.getOptionProperties("ep"); // Contrary to cli.getOptionValues(String), this is guaranteed non-null
            } catch (ParseException exp) {
                System.err.println("Error parsing the parameters: " + exp.getMessage());
                return;
            }
        }
        for (String fileName: compiledSet.inputFileNames) {
            context.parse( fileName );
            }
        context.waitUntilComplete(); // that also synchronizes all fields etc.
        compiledSet.compile(context);
        //generator.generate( context, compiledSet );
    }
}