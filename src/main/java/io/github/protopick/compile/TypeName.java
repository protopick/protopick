package io.github.protopick.compile;

import java.util.Arrays;
import java.util.List;
import io.github.protopick.parse.ParserConstants;
import io.github.protopick.parse.Token;

/** This represents a Message, sub-sub...Message, an Enum... */
public abstract class TypeName {
    public final TypeNameUse use;
    /*  A string (including empty), if
        - relevant and if
        - parsed already, or if assigned by the parser as default package.
        Null if not relevant, or not parsed/assigned. */
    private final TypeNamePackage packageName;
    public final String name;
    final TypeNameDefinition parentOrContext;
    /** "kind" (token.kind) of newTypeToken passed to the constructor, if it were non-null. Otherwise this is -1. */
    private final int newTypeTokenKind;

    private static List<Integer> ACCEPTABLE_TOP_LEVEL_TOKEN_KINDS= Arrays.asList( new Integer[] {
            ParserConstants.MESSAGE_TKN, ParserConstants.ENUM_TKN, ParserConstants.PACKAGE_TKN
    });
    /*
          @TODO Run against .proto, then remove this comment line: It may be null for usages that we're not interested in (for example other usages of ClassPart BNF rule). */
    TypeName (TypeNameUse givenUse, Token newTypeToken, TypeNameDefinition givenParent, TypeNamePackage givenPackage, String givenName) {
        use= givenUse;
        parentOrContext = givenParent;
        packageName = givenPackage;
        if (givenPackage!=null && givenPackage.use.definesNewType())
            throw new IllegalArgumentException("givenPackage must not define a new type.");
        if (givenName==null)
            throw new IllegalArgumentException("Type name must not be null.");
        if( givenName.isEmpty() && use!= TypeNameUse.PACKAGE )
            throw new IllegalArgumentException("Type name (other than a package name) must not be empty.");
        name = givenName;
        if ( (newTypeToken!=null) != use.registersWithToken() )
            throw new IllegalArgumentException("Param newTypeToken can be used when, and only when, defining not a new field, but a new type: " +name);
        if (use.definesNewType() && name.contains("."))
            throw new IllegalArgumentException("When defining not a new field, but a new type, the name must not contain a dot. However, it did: " +name);
        if (newTypeToken!=null)
            newTypeTokenKind = newTypeToken.kind;
        else {
            newTypeTokenKind = -1;
        }
        // We can't check this.packageName, since we can't require the package name in .proto.
        if (parentOrContext==null && use!=TypeNameUse.FIELD_PRIMITIVE
        && !ACCEPTABLE_TOP_LEVEL_TOKEN_KINDS.contains(newTypeTokenKind))
            throw new IllegalArgumentException("Top-level type must be a message, or an enum, but not a " +newTypeToken.image);
    }

    public String fullName() {
        if (use.mayBeRelative())
            throw new UnsupportedOperationException("Referral type names don't have full name defined.");
        if (parentOrContext != null)
            return parentOrContext.fullName() + '.' + name;
        else if (packageName != null)
            return packageName.name + '.' + name;
        else
            return name;
    }

    /** Equality is based on fullName(). That way you can create an instance with a dot-separated multi-level name in one go,
     * and it compares well to a cascaded instance, too. */
    @Override
    public boolean equals(Object other) {
        return other!=null && fullName().equals( ((TypeName)other).fullName() );
    }

    public int hashCode() {
        return fullName().hashCode();
    }
}
