package protongo.compile;

import java.util.Set;
import java.util.HashSet;

/** Information on a user-defined type. Used to generate output.
 *  Any one type is defined in the same file, hence the same Thread. Therefore no need to synchronize.
*/
public final class TypeDefinition {
    public final TypeNameDefinition typeNameDefinition;
    public boolean isEnum;
    public final Set<Field> fields= new HashSet<>();

    public TypeDefinition(TypeNameDefinition givenTypeNameDefinition) {
        typeNameDefinition= givenTypeNameDefinition;
    }
}
