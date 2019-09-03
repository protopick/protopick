package io.github.protopick.generate;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/** Instances generate non-empty toString() only when toString() hasn't been called for any other
 * instances in the same group. Use for example to generate "_id" field in Mongo DB schema at the top
 * level of a message (object), and also when reusing the inner objects in separate Mongo documents.
 * Otherwise "_id" would show up for every object level, but we want it at the top level only.
 * Not thread-safe, and to be put in objects used across multiple threads. Synchronizing can't "fix" it.
 * */
public final class FirstPerGroup {
    /** Map: Group => non-null if an instance was consumed already (and weakly referring to that instance).
     * We cannot have a lazy-like field on FirstPerGroup instance indicating whether that instance is
     * the one that was consumed as first in its group. Otherwise we'd have to reset those fields in clear().
     * */
    public/*private @TODO*/ static final ThreadLocal< WeakHashMap<Object, WeakReference<FirstPerGroup>> > consumed
        = ThreadLocal.withInitial( () -> new WeakHashMap<Object, WeakReference<FirstPerGroup>>() );

    public static void clear() {
        consumed.get().clear();
    }

    final Object group;
    final String content;
    final String otherwise;

    public FirstPerGroup(Object givenGroup, String givenContent ) {
        this( givenGroup, givenContent, "" );
    }
    public FirstPerGroup(Object givenGroup, String givenContent, String givenOtherwise ) {
        group= givenGroup;
        content= givenContent;
        otherwise= givenOtherwise;
    }

    /** Successive calls return the same => Hence we have the design that
     * - we don't share the instances across groups, and
     * - we keep a (weak) reference to the instance that was consumed (first) from its group. */
    public String toString() {
        WeakReference<FirstPerGroup> groupConsumedRef= consumed.get().get(group);
        FirstPerGroup groupConsumed= groupConsumedRef!=null
            ? groupConsumedRef.get()
            : null;
        assert groupConsumedRef==null || groupConsumed!=null;
        if( groupConsumed!=null ) {
            return groupConsumed==this
                ? content
                : otherwise;
        }
        else {
            consumed.get().put( group, new WeakReference<>(this) );
            return content;
        }
    }
}
