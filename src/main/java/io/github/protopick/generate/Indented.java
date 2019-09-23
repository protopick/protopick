package io.github.protopick.generate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Indented {
    public static final InheritableThreadLocal<String> INDENT_STEP= new InheritableThreadLocal<>();

    private List<Object> parts= new ArrayList<>();
    /** Whether this container has the same indent as its immediate parent (if any). */
    private boolean sameIndent= false;

    public Indented() {}
    public Indented(Object... given) {
        add(given);
    }
    /** This creates a new instance with the same indent as its immediate parent (if any).
     * There's no ability to make an existing instance have the same indent later, as that would allow the code to be confusing. */
    public static Indented newSameIndent() {
        Indented result= new Indented();
        result.sameIndent= true;
        return result;
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

    private static final Object SEPARATOR= new Object();

    /** Add a new line only if there are any previous items in this Indented, and
     * if the immediate previous item is not an instance of Indented.
     * If the next item is Indented, or if you call onNewLine() multiple times, that all generates one new line only.
     * Also, if there's no other content in this Indented, then onNewLine() doesn't add any trailing newline.
     * However, if the immediate previous item is not an instance of Indented but its toString()
     * ended with a new line (and any trailing whitespace characters), this method still adds a newline.
     * */
    public Indented onNewLine() {
        return add(SEPARATOR);
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    //public Indent reindent(Object) // This would remove the initial indent from every line
    private String toString( final String parentIdent, final String suggestedIndent, final String indentStep ) {
        final String indent= sameIndent
            ? parentIdent
            : suggestedIndent;
        final String innerIndent= indentStep+indent;
        final String newLineAndIndent= "\n" +indent;
        final StringBuilder builder= new StringBuilder();

        boolean lastPartIndented=false, lastPartSeparator=false, firstPart=true; //lastPart
        for( Iterator<Object> partIt=parts.iterator(); partIt.hasNext(); ) {
            Object part=partIt.next();
            if( part==SEPARATOR ) {
                if( !lastPartSeparator && partIt.hasNext() )
                    builder.append('\n');
            }
            else if( part instanceof Indented) {
                if( !firstPart && !lastPartSeparator )
                    builder.append('\n');
                builder.append( ( (Indented)part ).toString(indent, innerIndent, indentStep) );
            }
            else {
                String string= part!=null
                    ? part.toString()
                    : "null";
                if (lastPartIndented) // Because Indented doesn't append a new line at its end
                    builder.append('\n');
                if (lastPartIndented || lastPartSeparator || firstPart)
                    builder.append( indent );
                builder.append( string.replace("\n", newLineAndIndent) );
            }
            lastPartIndented= part instanceof Indented;
            lastPartSeparator= part==SEPARATOR;
            firstPart= false;
        }
        return builder.toString();
    }

    public String toString() {
        if (INDENT_STEP.get()==null)
            INDENT_STEP.set ("    ");
        return toString("", "", INDENT_STEP.get());
    }
}
