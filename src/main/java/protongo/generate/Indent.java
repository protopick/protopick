package protongo.generate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Indent {
    public static final InheritableThreadLocal<String> INDENT_STEP= new InheritableThreadLocal<>();

    private List<Object> parts= new ArrayList<>();

    public Indent() {}
    private static final Method ADD;
    static {
        try {
            ADD = Indent.class.getDeclaredMethod("add", Object[].class);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    public Indent(Object... given) {
        try {
            ADD.invoke(this, given);
        }
        catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Indent add(Object... given) {
        parts.addAll( Arrays.asList(given));
        return this;
    }

    public Indent newLine() {
        parts.add( "" );
        return this;
    }

    //public Indent reindent(Object) // This would remove the initial indent from every line

    public String toString( String indent, String indentStep ) {
        final String innerIndent= indent+indentStep;
        final StringBuilder builder= new StringBuilder();
        for( Object part: parts ) {
            if( part instanceof Indent ) {
                builder.append( ( (Indent)part ).toString(innerIndent, indentStep) );
            }
            else {
                String string= part!=null
                    ? part.toString()
                    : "null";
                builder.append( string.replaceAll("\n", indent) );
            }
        }
        return builder.toString();
    }

    public String toString() {
        if (INDENT_STEP.get()==null)
            INDENT_STEP.set ("    ");
        return toString("", INDENT_STEP.get());
    }
}