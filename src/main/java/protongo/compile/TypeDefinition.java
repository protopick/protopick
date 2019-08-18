package protongo.compile;

import java.util.Set;
import java.util.LinkedHashSet;
import protongo.parser.HandlingInstructed;
import protongo.parser.HandlingInstruction;

/** Information on a user-defined type. Used to generate output.
 *  Any one type is defined in the same file, hence the same Thread. Therefore no need to synchronize.
*/
public final class TypeDefinition implements HandlingInstructed {
    public final TypeNameDefinition typeNameDefinition;
    public boolean isEnum;
    public final Set<Field> fields= new LinkedHashSet<>();

    public TypeDefinition(TypeNameDefinition givenTypeNameDefinition) {
        typeNameDefinition= givenTypeNameDefinition;
    }

    private HandlingInstruction instruction;
    public HandlingInstruction getInstruction() { return instruction; }
    public void setInstruction(HandlingInstruction given) { instruction = given; }
}
