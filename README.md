This parses `.proto` files and generates Mongo DB schema.

Based on https://github.com/Tand0/javaccProto.

# Run
To invoke directly, run:
`java -cp build/libs/protongo.jar -cp apache-commons-cli.jar protongo.compile.Run`. That gives you help on the parameters.

To invoke from Gradle, run:
`gradle run --args='actual arguments'`

For help on the actual arguments, invoke:
`gradle run` or `gradle run --args='--help'`

For example
`gradle clean build  run --args='-I proto-test -g protongo.generate.MessagesMongo -ep OuterMost=outer_most.js relative-sub-types.proto'`

We don't support reading from standard input or writing to standard output. If you need that, pass -f /dev/stdin (and --I /dev), and/or -o /dev and have plugin that specifies output filename to be stdout.
