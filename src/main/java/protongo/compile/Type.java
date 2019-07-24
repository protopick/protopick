package protongo.compile;

import java.util.Set;
import java.util.HashSet;

/** Information on a user-defined type. Used to generate output.
 *  Any one type is defined in the same file, hence the same Thread. Therefore no need to synchronize.
*/
public final class Type {
    public final TypeNameDefinition definitionType;
    public boolean isEnum;
    public final Set<Field> fields= new HashSet<>();

    public Type (TypeNameDefinition givenDefinitionType) {
        definitionType= givenDefinitionType;
    }
}
