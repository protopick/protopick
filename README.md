This parses `.proto` files. It produces output based on custom generators. It comes with a generator of Mongo DB schema.

Based on https://github.com/Tand0/javaccProto.

# Build
`gradle clean build`

# Run
To invoke directly,
 1. Download  
 2. Run: `java -cp build/libs/protopick.jar -cp apache-commons-cli.jar io.github.protopick.compile.Run`. That gives you help on the parameters.

To invoke from Gradle, run:
`gradle run --args='actual arguments'`
For example:
```
gradle run "--args=-I proto-test -f User.proto -p io.github.protopick.generate.MessagesMongo" --quiet > User.js
echo "db = db.getSiblingDB('User');" | cat /dev/stdin User.js | mongo
```

For help on the actual arguments, invoke:
`gradle run` or `gradle run --args='--help'`

# Limitations
Far from complete. It's not complete, but it supports `enum, message, repeated` and basic built-in types (`string, int32, int64, bool`).

For now it fails if there's an external `import` or type (from Google). So comment out imports like `google/protobuf/field_mask.proto` and their usage.

The plugin API is likely to change.

We don't support reading from standard input or writing to standard output. If you need that, pass `-f /dev/stdin` (and `--I /dev`), and/or `-o /dev` and (TODO:) use a plugin that specifies output filename to be `stdout`.

For later:
`gradle run --args='-I proto-test -g io.github.protopick.generate.MessagesMongo -ep OuterMost=outer_most.js relative-sub-types.proto'`