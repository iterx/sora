package org.iterx.sora.collection.queue;

import org.iterx.sora.collection.Arrays;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class AbstractCircularBlockingQueue<T> implements CircularBlockingQueue<T> {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private final Lock dequeueLock;
    private final Lock enqueueLock;
    private final Condition dequeueCondition;
    private final Condition enqueueCondition;

    private final T[] queue;
    private final int capacity;
    private final boolean overwrite;

    private volatile Iterator<T> iterator;
    private volatile int dequeueWaiters = 0;
    private volatile int enqueueWaiters = 0;
    private volatile int writeIndex = -1;
    private volatile int readIndex = -1;
    private volatile int removeIndex = -1;

    private volatile long sequence = 0;

    AbstractCircularBlockingQueue(final Lock enqueueLock,
                                  final Lock dequeueLock,
                                  final int capacity,
                                  final boolean overwrite) {
        this.enqueueCondition = enqueueLock.newCondition();
        this.dequeueCondition = dequeueLock.newCondition();
        this.queue = Arrays.newArray(Object.class, capacity);
        this.enqueueLock = enqueueLock;
        this.dequeueLock = dequeueLock;
        this.capacity = capacity;
        this.overwrite = overwrite;
    }

    public long index() {
        return sequence;
    }

    public boolean offer(final T value) {
        if(remainingCapacity() != 0) {
            enqueue(value);
            return true;
        }
        return false;
    }

    public boolean offer(final T value, final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        if(remainingCapacity() == 0) awaitDequeue(timeout, timeUnit);
        enqueue(value);
        return true;
    }

    public void put(final T value) throws InterruptedException {
        if(remainingCapacity() == 0) awaitDequeue();
        enqueue(value);
    }

    public boolean add(final T value) {
        if(remainingCapacity() != 0) {
            enqueue(value);
            return true;
        }
        return false;
    }

    public boolean addAll(final Collection<? extends T> values) {
        if(remainingCapacity() < values.size()) {
            for(final T value : values) enqueue(value);
            return true;
        }
        throw new IllegalStateException();
    }

    public T get(final long index) {
        if(!isWithinRange(index)) throw new IllegalArgumentException();
        return queue[(readIndex - (int) (sequence - index))];
    }

    public void rewind(final long index) {
        if(!isWithinRange(index)) throw new IllegalArgumentException();
        final int delta  = (int) (sequence - index);
        readIndex -= delta;
        sequence -= delta;
    }

    public T peek() {
        return (isEmpty())? null : queue[(1 + readIndex) % capacity];
    }

    public T poll() {
        return (!isEmpty())? dequeue() : null;
    }

    public T poll(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        if(isEmpty()) awaitEnqueue(timeout, timeUnit);
        return dequeue();
    }

    public T take() throws InterruptedException {
        if(isEmpty()) awaitEnqueue();
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
        final int count = Math.min(size, readIndex - removeIndex);
        for(int i = count; i-- != 0;) values.add(remove());
        return count;
    }

    public T remove() {
        if(removeIndex == readIndex) throw new NoSuchElementException();
        final T value = queue[(++removeIndex % capacity)];
        signalDequeue();
        return value;
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
        enqueueLock.lock();
        try {
            dequeueLock.lock();
            try {
                Arrays.clear(queue);
                sequence = 0;
                readIndex = writeIndex = -1;
                dequeueCondition.signalAll();
                enqueueCondition.signalAll();
                enqueueWaiters = dequeueWaiters = 0;
            }
            finally {
                dequeueLock.lock();
            }
        }
        finally {
            enqueueLock.unlock();
        }
    }

    public int size() {
        return writeIndex - readIndex;
    }

    public int remainingCapacity() {
        return (overwrite)? capacity - (writeIndex - readIndex) : capacity - (writeIndex - removeIndex);
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
        return (iterator != null)? iterator : (iterator = new CircularBlockingQueueIterator());
    }

    public Object[] toArray() {
        return toArray(EMPTY_ARRAY);
    }

    public <T> T[] toArray(final T[] array) {
        final int size = size();
        final T[] target = (array.length < size)? Arrays.<T>newArray(Object.class, size) : array;
        if(writeIndex < readIndex) {
            System.arraycopy(queue, 0, target, 0, readIndex + size);
            System.arraycopy(queue, readIndex, target, readIndex + size, queue.length);
        }
        else System.arraycopy(queue, readIndex, target, 0, size);
        return target;
    }

    private boolean isWithinRange(final long index) {
        final long delta = sequence - index;
        return (delta > 0 || delta < (capacity - (writeIndex - removeIndex)));
    }

    private void enqueue(final T value)
    {
        queue[(++writeIndex % capacity)] = value;
        if(overwrite && writeIndex == removeIndex + capacity) ++removeIndex;
        signalEnqueue();
    }

    private T dequeue()
    {
        final T value = queue[(++readIndex % capacity)];
        sequence++;
        if(overwrite) signalDequeue();
        return value;
    }

    private void awaitDequeue() throws InterruptedException {
        dequeueLock.lock();
        try {
            if(remainingCapacity() == 0) {
                dequeueWaiters++;
                dequeueCondition.await();
            }
        }
        finally {
            dequeueLock.unlock();
        }
    }

    private void awaitDequeue(final long timeout, final TimeUnit timeUnit) throws InterruptedException{
        dequeueLock.lock();
        try {
            if(remainingCapacity() == 0) {
                dequeueWaiters++;
                dequeueCondition.await(timeout, timeUnit);
            }
            }
        finally {
            dequeueLock.unlock();
        }
    }

    private void signalDequeue() {
        if(dequeueWaiters != 0) {
            dequeueLock.lock();
            try {
                if(dequeueWaiters != 0) {
                    dequeueCondition.signal();
                    dequeueWaiters--;
                }
            }
            finally {
                dequeueLock.unlock();
            }
        }
    }

    private void awaitEnqueue() throws InterruptedException {
        enqueueLock.lock();
        try {
            if(isEmpty())
            {
                enqueueWaiters++;
                enqueueCondition.await();
            }
        }
        finally {
            enqueueLock.unlock();
        }
    }

    private void awaitEnqueue(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        enqueueLock.lock();
        try {
            if(isEmpty())
            {
                enqueueWaiters++;
                enqueueCondition.await(timeout, timeUnit);
            }
        }
        finally {
            enqueueLock.unlock();
        }
    }

    private void signalEnqueue() {
        if(enqueueWaiters != 0) {
            enqueueLock.lock();
            try {
                if(enqueueWaiters != 0) {
                    enqueueCondition.signal();
                    enqueueWaiters--;
                }
            }
            finally {
                enqueueLock.unlock();
            }
        }

    }

    private class CircularBlockingQueueIterator implements Iterator<T> {

        public boolean hasNext() {
            return !AbstractCircularBlockingQueue.this.isEmpty();
        }

        public T next() {
            return AbstractCircularBlockingQueue.this.poll();
        }

        public void remove() {
            AbstractCircularBlockingQueue.this.remove();
        }
    }
}