package org.iterx.sora.collection.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class MultiProducerSingleConsumerCircularBlockingQueue<T> extends AbstractCircularBlockingQueue<T> {

    private final Lock enqueueLock;

    public MultiProducerSingleConsumerCircularBlockingQueue(final int capacity, final boolean overwrite) {
        this(new ReentrantLock(), new ReentrantLock(), capacity, overwrite);
    }

    private MultiProducerSingleConsumerCircularBlockingQueue(final Lock enqueueLock,
                                                             final Lock dequeueLock,
                                                             final int capacity,
                                                             final boolean overwrite) {
        super(enqueueLock, dequeueLock, capacity, overwrite);
        this.enqueueLock = enqueueLock;
    }

    @Override
    public boolean offer(final T value) {
        enqueueLock.lock();
        try {
            return super.offer(value);
        }
        finally {
            enqueueLock.unlock();
        }
    }

    @Override
    public boolean offer(final T value, final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        enqueueLock.lock();
        try {
            return super.offer(value, timeout, timeUnit);
        }
        finally {
            enqueueLock.unlock();
        }
    }

    @Override
    public void put(final T value) throws InterruptedException {
        enqueueLock.lock();
        try {
            super.put(value);
        }
        finally {
            enqueueLock.unlock();
        }
    }

    @Override
    public boolean add(final T value) {
        enqueueLock.lock();
        try {
            return super.add(value);
        }
        finally {
            enqueueLock.unlock();
        }
    }

    @Override
    public boolean addAll(final Collection<? extends T> values) {
        enqueueLock.lock();
        try {
            return super.addAll(values);
        }
        finally {
            enqueueLock.unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        enqueueLock.lock();
        try {
            return super.iterator();
        }
        finally {
            enqueueLock.unlock();
        }
    }
}