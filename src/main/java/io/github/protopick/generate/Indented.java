package io.github.protopick.generate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Indented {
    public static final InheritableThreadLocal<String> INDENT_STEP= new InheritableThreadLocal<>();

    private List<Object> parts= new ArrayList<>();

    public Indented() {}
    public Indented(Object... given) {
        add(given);
    }

    public Indented addArray(Object given[]) {
        parts.addAll( Arrays.asList(given));
        return this;
    }

    public Indented add(Object... given) {
        return addArray(given);
    }

    /** Not returning this instance, because chaining such calls would be confusing. */
    public void prepend(Object o) {
        parts.add( 0, o );
    }

    /** Add a new line only if there are any previous items in this Indented, and
     * if the immediate previous item is not an instance of Indented.
     * However, if the immediate previous item is not an instance of Indented but its toString()
     * ended with a new line (and any trailing white characters), this method still adds a newline.
     * Don't call if the next item to be added may be Indented, otherwise you'll have two new lines.
     * Beware: If you call prepend(Object) later, the previous effect of onNewLine() will NOT be adjusted.
     * */
    public Indented onNewLine() {
        if( !parts.isEmpty() && !(parts.get(parts.size()-1) instanceof io.github.protopick.generate.Indented) )
            parts.add( "\n" );
        return this;
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    //public Indent reindent(Object) // This would remove the initial indent from every line
    private String toString( String indent, String indentStep ) {
        final String innerIndent= indentStep+indent;
        final String newLineAndIndent= "\n" +indent;
        final StringBuilder builder= new StringBuilder();

        boolean lastPartSpecial=false, firstPart=true; //lastPart
        for( Object part: parts ) {
            if( part instanceof Indented) {
                if (!firstPart)
                    builder.append("\n");
                builder.append( ( (Indented)part ).toString(innerIndent, indentStep) );
            }
            else {
                String string= part!=null
                    ? part.toString()
                    : "null";
                if (lastPartSpecial)
                    builder.append("\n");
                if (lastPartSpecial || firstPart)
                    builder.append( indent );
                builder.append( string.replace("\n", newLineAndIndent) );
            }
            lastPartSpecial= part instanceof Indented;
            firstPart= false;
        }
        return builder.toString();
    }

    public String toString() {
        if (INDENT_STEP.get()==null)
            INDENT_STEP.set ("    ");
        return toString("", INDENT_STEP.get());
    }
}
