package org.iterx.sora.collection.list;

import org.iterx.sora.collection.List;
import org.iterx.sora.collection.Record;

import java.util.ArrayList;

public final class RandomAccessList<E> extends ArrayList<E> implements List<E> {

    public RandomAccessList() {
        super();
    }

    public RandomAccessList(final int capacity) {
        super(capacity);
    }

    public Record<E> head() {
        throw new UnsupportedOperationException();
    }

    public Record<E> tail() {
        throw new UnsupportedOperationException();
    }
}
