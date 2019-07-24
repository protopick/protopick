This parses `.proto` files and generates Mongo DB schema.

Based on https://github.com/Tand0/javaccProto.

# Run
To invoke directly, run:
`java -cp build/libs/protongo.jar -cp apache-commons-cli.jar protongo.compile.Run`. That gives you help on the parameters.

To invoke from Gradle, run:
`gradle run --args='actual arguments'`

For help on the actual arguments, invoke:
`gradle run` or `gradle run --args='--help'`
