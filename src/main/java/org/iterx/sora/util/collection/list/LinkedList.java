package org.iterx.sora.util.collection.list;

import org.iterx.sora.util.collection.List;
import org.iterx.sora.util.collection.Record;

public final class LinkedList<E> extends java.util.LinkedList<E> implements List<E> {

    public LinkedList() {
    }

    public Record<E> head() {
        throw new UnsupportedOperationException();
    }

    public Record<E> tail() {
        throw new UnsupportedOperationException();
    }
}
