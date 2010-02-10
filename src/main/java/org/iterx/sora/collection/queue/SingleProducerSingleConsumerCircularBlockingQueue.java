package org.iterx.sora.collection.queue;

import java.util.concurrent.locks.ReentrantLock;

public class SingleProducerSingleConsumerCircularBlockingQueue<T> extends AbstractCircularBlockingQueue<T> {

    public SingleProducerSingleConsumerCircularBlockingQueue(final int capacity, final boolean overwrite) {
        super(new ReentrantLock(), new ReentrantLock(), capacity, overwrite);
    }
}
