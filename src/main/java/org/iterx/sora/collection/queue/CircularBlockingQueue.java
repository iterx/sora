package org.iterx.sora.collection.queue;

public interface CircularBlockingQueue<T> extends BlockingQueue<T> {

    long index();

    T get(long index);

    void rewind(long index);

    int capacity();
}
