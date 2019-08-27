package io.github.protopick.compile;

import java.io.File;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
// Watch out: Both Apache Commons CLI, and JavaCC-generated code, define class "ParseException"
import org.apache.commons.cli.ParseException;
import io.github.protopick.generate.Plugin;
import io.github.protopick.parse.ParserContext;

public class Run {
    private static Options createOptions() {
        final Options options = new Options(); // options will be shown in alphabetical order, case insensitive; NOT (necessarily) in the same order as added below, but let's keep them in the same order below
        Option exportsOpt= Option.builder("ep").longOpt( "export_pair")
            .numberOfArgs(2) //@TODO test with hasArgs()
            .valueSeparator()
            .desc( "Export pair: package.qualified.type=output/file-with-extension. You can pass multiple, "
                        +"one '-ep pair' or '--export_pair pair' per each type to be exported.")
            .build();
        options.addOption(exportsOpt);

        Option filesOpt= Option.builder("f").longOpt("files")
           .desc(".proto file(s). Must exist in one of the import path(s).")
           .hasArgs().required().build();
        options.addOption(filesOpt);

        Option helpOpt= Option.builder("h").longOpt("help")
            .desc("Show this help.").build();
        options.addOption(helpOpt);

        // Option -I and --proto_path is based on `protoc`. You can repeat it, passing a different value each
        // time. It's optional. However, even if you do import any files from the same folder as the 'start' file (on
        // which you're invoking this), you must pass '-IPATH' or `--proto_path` for that 'start' folder, too.
        Option protoPathOpt= Option.builder("I").longOpt( "proto_path" )
           .desc( "Import path(s)." )
           .hasArgs() // Since we use .hasArgs(), we have to have an option with .haArgs() for the .proto file(s), too. We can't use cli.getArgs() to get the .proto file(s) from the rest of the arguments (after any options), because hasArgs() would consume them (if this were the last option). But this choice makes it more robust. It also requires the commandline parameters to be more intentional.
           .required().build();
        options.addOption (protoPathOpt);

        // Later: '-ei item' or '--export_item item'
        // together with '-ee extension' or '--export_extension extension' and
        // '-en sand-wich', '-en under_score' or '-en lowercase' (or --export_naming with the same values)
        // When exporting, the output filenames are based on Protoc 'package'. They're not based on the location of the .proto files.
        Option outputOpt= Option.builder("o").longOpt("out")
            .desc( "Output folder. If not present, using the current directory. This is prefixed "
                           +"in front of each export subpath from 'e' or '--export' parameter(s).")
            .hasArg().build();
        options.addOption(outputOpt);

        Option pluginsOpt= Option.builder("p").longOpt("plugin")
            .desc( "Plugin(s). Full, package-qualified Java class name(s).")
            .hasArgs().required().build();
        options.addOption(pluginsOpt);

        /* @TODO
        Option instructedOnly= Option
            .builder("io")
            .longOpt("instructed_only")
            .desc("Whether to generate only for entries that have a 'handling instruction'.")
            .build();*/
        return options;
    }

    public static void main(String... args) {
        final ParserContext context= new ParserContext();
        final CompiledSet compiledSet= new CompiledSet(context);
        final List<Plugin> plugins= new java.util.ArrayList<>(); // Not a Set, because we apply them in order
        {
            final Options options= createOptions();
            CommandLineParser parser = new DefaultParser();
            CommandLine cli= null;
            if( args.length>0 ) { // If no args at all, then don't parse and don't show any parsing errors, but show help
                try {
                    cli = parser.parse(options, args);
                    if (cli.getArgs().length>0)
                        throw new ParseException("Unexpected value(s) at the end: " +cli.getArgList());
                    compiledSet.inputFileNames= cli.getOptionValues('f');
                } catch (ParseException exp) {
                    Options helpOnlyOptions= new Options();
                    helpOnlyOptions.addOption( options.getOption("h") );
                    // Alternatively we could use a regex: (\s|^)(-h|--help)(\s|$)
                    CommandLineParser helpOnlyParser = new DefaultParser();
                    boolean showParseErrors= false;
                    try {
                        CommandLine helpOnlyCli= helpOnlyParser.parse(helpOnlyOptions, args);
                        // If the user provided only -h or --help, then don't show parsing errors
                        showParseErrors= !helpOnlyCli.hasOption('h') || helpOnlyCli.getArgs().length>0;
                    }
                    catch (ParseException exp2) {
                        // The user didn't provide -h or --help, or they mixed it with other options
                        showParseErrors= true;
                    }
                    if (showParseErrors)
                        System.err.println("Error parsing the parameters: " + exp.getMessage());
                }
            }
            if (cli==null || cli.hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                String header= "Most options are multi-value. Some accept multiple values for the same option. Others accept one pair per option, but you can repeat the option with different pairs.";
                String footer= "<footer @TODO>";
                formatter.printHelp( "gradle run --args='args...' OR: java io.github.protopick", header, options, footer, true );
                return;
            }

            // Don't use proto_path.getValue(), it was null!
            context.includePaths= cli.getOptionValues('I');
            //System.out.println( "context.includePaths: " +Arrays.asList(context.includePaths));
            if (context.includePaths==null) // if the option is not present, this not an empty array, but null!
                throw new IllegalArgumentException("Must pass some -I or --proto_path, even for the folder(s) where the start file(s) are.");

            String pluginClassNames[]= cli.getOptionValues('p');
            if (pluginClassNames==null)
                throw new IllegalArgumentException("Must pass a -p or --plugin option with a generator's full class name.");
            for (String pluginClassName: pluginClassNames) {
                try {
                    Class<Plugin> pluginClass = (Class<Plugin>) Class
                            .forName(pluginClassName);
                    plugins.add( pluginClass.newInstance() );
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Couldn't load a plugin class " + pluginClassName, e);
                }
            }
            compiledSet.out= cli.getOptionValue('o');
            if (compiledSet.out == null)
                compiledSet.out = "";
            else if (!compiledSet.out.endsWith(File.separator))
                compiledSet.out += File.separatorChar;

            compiledSet.exportItems= cli.getOptionProperties("ep"); // Contrary to cli.getOptionValues(String), this is guaranteed non-null
        }
        for (String fileName: compiledSet.inputFileNames) {
            context.parse( fileName );
        }
        context.waitUntilComplete(); // that also synchronizes all fields etc.
        compiledSet.compile();
        for (Plugin plugin: plugins) {
            plugin.generate(context, compiledSet);
        }
    }
}