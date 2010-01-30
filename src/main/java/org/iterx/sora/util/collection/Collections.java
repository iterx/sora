package org.iterx.sora.util.collection;

import org.iterx.sora.util.collection.list.RandomAccessList;
import org.iterx.sora.util.collection.set.HashSet;

import java.util.List;

public final class Collections {

    private Collections() {
    }

    public static <V> List<V> newList(final V... values) {
        final List<V> list = new RandomAccessList<V>(values.length);
        for(final V value : values) list.add(value);
        return list;
    }

    public static <V> Set<V> newSet(final V... values) {
        final Set<V> set = new HashSet<V>();
        for(final V value : values) set.add(value);
        return set;
    }
}
