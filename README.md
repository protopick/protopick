This parses `.proto` files. It produces output based on custom generators. It comes with a generator of Mongo DB schema.

Based on https://github.com/Tand0/javaccProto.

# Build
`gradle clean build`

# Run
To invoke directly, run:
`java -cp build/libs/protopick.jar -cp apache-commons-cli.jar io.github.protopick.compile.Run`. That gives you help on the parameters.

To invoke from Gradle, run:
`gradle run --args='actual arguments'`

For help on the actual arguments, invoke:
`gradle run` or `gradle run --args='--help'`

For example
`gradle run --args='-I proto-test -g io.github.protopick.generate.MessagesMongo -ep OuterMost=outer_most.js relative-sub-types.proto'`

We don't support reading from standard input or writing to standard output. If you need that, pass `-f /dev/stdin` (and `--I /dev`), and/or `-o /dev` and (TODO:) use a plugin that specifies output filename to be `stdout`.
