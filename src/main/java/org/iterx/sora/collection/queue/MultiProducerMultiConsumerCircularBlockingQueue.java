package org.iterx.sora.collection.queue;

import org.iterx.sora.util.Concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Concurrent
public final class MultiProducerMultiConsumerCircularBlockingQueue<T> extends AbstractCircularBlockingQueue<T> {

    private final Lock lock;

    public MultiProducerMultiConsumerCircularBlockingQueue(final int capacity,
                                                           final boolean overwrite) {
        this(new ReentrantLock(), capacity, overwrite);
    }

    private MultiProducerMultiConsumerCircularBlockingQueue(final Lock lock,
                                                            final int capacity,
                                                            final boolean overwrite) {
        super(lock, lock, capacity, overwrite);
        this.lock = lock;
    }

    @Override
    public boolean offer(final T value) {
        lock.lock();
        try {
            return super.offer(value);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean offer(final T value, final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        lock.lock();
        try {
            return super.offer(value, timeout, timeUnit);
        }
        finally {
            lock.unlock();
        }
    }


    @Override
    public void put(final T value) throws InterruptedException {
        lock.lock();
        try {
            super.put(value);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(final T value) {
        lock.lock();
        try {
            return super.add(value);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(final Collection<? extends T> values) {
        lock.lock();
        try {
            return super.addAll(values);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        lock.lock();
        try {
            return super.iterator();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public T get(final long index) {
        lock.lock();
        try {
            return super.get(index);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void rewind(final long index) {
        lock.lock();
        try {
            super.rewind(index);
        }
        finally {
            lock.unlock();
        }
    }


    @Override
    public T poll() {
        lock.lock();
        try {
            return super.poll();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public T poll(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        lock.lock();
        try {
            return super.poll(timeout, timeUnit);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public T take() throws InterruptedException {
        lock.lock();
        try {
            return super.take();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public T element() {
        lock.lock();
        try {
            return super.element();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public int drainTo(final Collection<? super T> values, final int size) {
        lock.lock();
        try {
            return super.drainTo(values, size);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public T remove() {
        lock.lock();
        try {
            return super.remove();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(final Object object) {
        lock.lock();
        try {
            return super.contains(object);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAll(final Collection<?> objects) {
        lock.lock();
        try {
            return super.containsAll(objects);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        lock.lock();
        try {
            return super.toArray(array);
        }
        finally {
            lock.unlock();
        }
    }
}