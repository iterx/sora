package org.iterx.sora.collection.queue;

import java.util.concurrent.locks.ReentrantLock;

public final class SingleProducerSingleConsumerBlockingQueue<T> extends AbstractBlockingQueue<T> {

    public SingleProducerSingleConsumerBlockingQueue(final int capacity) {
        super(new ReentrantLock(), new ReentrantLock(), capacity);
    }
}