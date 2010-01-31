package org.iterx.sora.collection.queue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class MultiProducerSingleConsumerBlockingQueue<T> implements BlockingQueue<T> {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private final Lock writeLock;
    private final Condition onReadCondition;
    private final Condition onWriteCondition;
    private final T[] queue;
    private final int capacity;

    private volatile Iterator<T> iterator;
    private volatile int onReadWaiters = 0;
    private volatile int onWriteWaiters = 0;
    private volatile int writeIndex = -1;
    private volatile int readIndex = -1;

    public MultiProducerSingleConsumerBlockingQueue(final int capacity) {
        this.writeLock = new ReentrantLock();
        this.onWriteCondition = writeLock.newCondition();
        this.onReadCondition = writeLock.newCondition();
        this.queue = this.<T>newArray(capacity);
        this.capacity = capacity;
    }
       
    public boolean offer(final T value) {
        writeLock.lock();
        try {
            if(remainingCapacity() != 0) {
                enqueue(value);
                return true;
            }
            return false;
        }
        finally {
            writeLock.unlock();
        }
    }

    public boolean offer(final T value, final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        writeLock.lock();
        try {
            if(remainingCapacity() == 0) {
                onReadWaiters++;
                onReadCondition.await(timeout, timeUnit);
            }
            enqueue(value);
            return true;
        }
        finally {
            writeLock.unlock();
        }
    }

    public void put(final T value) throws InterruptedException {
        writeLock.lock();
        try {
            if(remainingCapacity() == 0) {
                onReadWaiters++;
                onReadCondition.await();
            }
            enqueue(value);
        }
        finally {
            writeLock.unlock();
        }
    }

    public boolean add(final T value) {
        writeLock.lock();
        try {
            if(remainingCapacity() != 0) {
                enqueue(value);
                return true;
            }
            throw new IllegalStateException();
        }
        finally {
            writeLock.unlock();
        }
    }

    public boolean addAll(final Collection<? extends T> values) {
        writeLock.lock();
        try {
            if(remainingCapacity() < values.size()) {
                for(final T value : values) enqueue(value);
                return true;
            }
            throw new IllegalStateException();
        }
        finally {
            writeLock.unlock();
        }
    }

    public T peek() {
        return (isEmpty())? null : queue[(1 + readIndex) % capacity];
    }

    public T poll() {
        return (!isEmpty())? dequeue() : null;
    }

    public T poll(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        if(isEmpty()) await(timeout, timeUnit);
        return dequeue();
    }

    public T take() throws InterruptedException {
        if(isEmpty()) await();
        return dequeue();
    }

    public T element() {
        if(isEmpty()) throw new NoSuchElementException();
        return queue[(1 + readIndex) % capacity];
    }

    public int drainTo(final Collection<? super T> values) {
        return drainTo(values, values.size());
    }

    public int drainTo(final Collection<? super T> values, final int size) {
        final int count = Math.min(size, size());
        for(int i = count; i-- != 0;) values.add(dequeue());
        return count;
    }

    public T remove() {
        if(isEmpty()) throw new NoSuchElementException();
        return dequeue();
    }

    public boolean remove(final Object object) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(final Collection<?> objects) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection<?> objects) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        writeLock.lock();
        try {
            Arrays.fill(queue, null);
            readIndex = writeIndex = -1;
            onReadCondition.signalAll();
            onWriteCondition.signalAll();
            onWriteWaiters = onReadWaiters = 0;        
        }
        finally {
            writeLock.unlock();
        }
    }

    public int size() {
        return writeIndex - readIndex;
    }

    public int remainingCapacity() {
        return capacity - (writeIndex - readIndex);
    }

    public boolean isEmpty() {
        return readIndex == writeIndex;
    }

    public boolean contains(final Object object) {
        for(int i = readIndex; i < writeIndex; i++) {
            if(object.equals(queue[i % capacity])) return true;
        }
        return false;
    }

    public boolean containsAll(final Collection<?> objects) {
        OUTER: for(final Object object : objects) {
            for(int i = readIndex; i < writeIndex; i++) {
                if(object.equals(queue[i % capacity])) continue OUTER;
            }
            return false;
        }
        return true;
    }

    public Iterator<T> iterator() {
        writeLock.lock();
        try {
            return (iterator != null)? iterator : (iterator = new MultiProducerSingleConsumerBlockingQueueIterator());
        }
        finally {
            writeLock.unlock();
        }
    }

    public Object[] toArray() {
        return toArray(EMPTY_ARRAY);
    }

    public <T> T[] toArray(final T[] array) {
        final int size = size();
        final T[] target = (array.length < size)? this.<T>newArray(size) : array;
        if(writeIndex < readIndex) {
            System.arraycopy(queue, 0, target, 0, readIndex + size);
            System.arraycopy(queue, readIndex, target, readIndex + size, queue.length);
        }
        else System.arraycopy(queue, readIndex, target, 0, size);
        return target;
    }

    private void enqueue(final T value)
    {
        queue[(++writeIndex % capacity)] = value;
        if(onWriteWaiters != 0) {
            onWriteCondition.signal();
            onWriteWaiters--;
        }
    }

    private T dequeue()
    {
        final int index = ++readIndex % capacity;
        final T value = queue[index];
        queue[index] = null;
        if(onReadWaiters != 0) signal();
        return value;

    }

    private void signal() {
        writeLock.lock();
        try {
            onReadCondition.signal();
            onReadWaiters--;
        }
        finally {
            writeLock.unlock();
        }
    }

    private void await() throws InterruptedException {
        writeLock.lock();
        try {
            if(isEmpty())
            {
                onWriteWaiters++;
                onWriteCondition.await();
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    private void await(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        writeLock.lock();
        try {
            if(isEmpty())
            {
                onWriteWaiters++;
                onWriteCondition.await(timeout, timeUnit);
            }
        }
        finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] newArray(final int capacity) {
        return (T[]) new Object[capacity];
    }

    private class MultiProducerSingleConsumerBlockingQueueIterator implements Iterator<T> {

        public boolean hasNext() {
            return !MultiProducerSingleConsumerBlockingQueue.this.isEmpty();
        }

        public T next() {
            return MultiProducerSingleConsumerBlockingQueue.this.remove();
        }

        public void remove() {
            MultiProducerSingleConsumerBlockingQueue.this.remove();
        }
    }
}
