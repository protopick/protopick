package protongo.compile;

import java.util.Set;
import java.util.HashSet;

public final class Type {
    public boolean isEnum;
    public final Set<Field> fields= new HashSet<>();
}
