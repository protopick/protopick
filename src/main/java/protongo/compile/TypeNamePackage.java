package protongo.compile;

import protongo.parser.ParserConstants;
import protongo.parser.Token;

public final class TypeNamePackage extends TypeName {
    /** A name of a package. Usage: Store this instance in `packageName` in JJT grammar.
     * @param newTypeToken Token ("package" only).
     */
    public TypeNamePackage (Token packageToken, String givenPackageName) {
        super (TypeNameUse.PACKAGE, packageToken, null, null, givenPackageName);
        if (packageToken.kind!= ParserConstants.PACKAGE_TKN)
            throw new IllegalArgumentException("Use only for 'package' headings.");
    }
}
