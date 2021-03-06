package org.iterx.sora.collection.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class MultiProducerSingleConsumerBlockingQueue<T> extends AbstractBlockingQueue<T> {

    private final Lock enqueueLock;

    public MultiProducerSingleConsumerBlockingQueue(final int capacity) {
        this(new ReentrantLock(), new ReentrantLock(), capacity);
    }

    private MultiProducerSingleConsumerBlockingQueue(final Lock enqueueLock,
                                                    final Lock dequeueLock,
                                                    final int capacity) {
        super(enqueueLock, dequeueLock, capacity);
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
