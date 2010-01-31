package org.iterx.sora.kernel.actor.receiver;

import org.iterx.sora.collection.list.RandomAccessList;

import java.util.List;

public abstract class AbstractBatchReceiver<T> implements Receiver<T> {

    private final List<T> objects;
    private final int capacity;

    public AbstractBatchReceiver(final int capacity) {
        this.objects = new RandomAccessList<T>();
        this.capacity = capacity;
    }

    public final void receive(final T object) {
        objects.add(object);
        if(isAtCapacity()) flush();
    }

    public void flush() {
        try {
            receive(objects);
        }
        finally {
            objects.clear();
        }
    }

    protected abstract void receive(final List<T> objects);

    private boolean isAtCapacity() {
        return capacity == objects.size();
    }
}
