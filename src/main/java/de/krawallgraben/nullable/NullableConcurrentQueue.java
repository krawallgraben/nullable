package de.krawallgraben.nullable;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A thread-safe queue that allows `null` elements.
 *
 * <p>This implementation wraps a {@link ConcurrentLinkedQueue} to support `null` values, which are
 * typically not allowed in standard concurrent collections.
 *
 * @param <E> the type of elements held in this queue
 */
@SuppressWarnings("serial")
public class NullableConcurrentQueue<E> extends AbstractQueue<E> implements Queue<E>, Serializable {

    /** Internal queue that stores masked values. */
    private final ConcurrentLinkedQueue<Object> internalQueue;

    /** Placeholder for `null`. */
    private enum NullPlaceholder {
        INSTANCE;

        @Override
        public String toString() {
            return "null";
        }
    }

    private static Object mask(Object value) {
        return value == null ? NullPlaceholder.INSTANCE : value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmask(Object value) {
        return value == NullPlaceholder.INSTANCE ? null : (T) value;
    }

    /** Constructs an empty queue. */
    public NullableConcurrentQueue() {
        this.internalQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Constructs a queue containing the elements of the specified collection, in the order they are
     * returned by the collection's iterator.
     *
     * @param c the collection whose elements are to be placed into this queue
     */
    public NullableConcurrentQueue(Collection<? extends E> c) {
        this.internalQueue = new ConcurrentLinkedQueue<>();
        if (c != null) {
            for (E e : c) {
                add(e);
            }
        }
    }

    @Override
    public Iterator<E> iterator() {
        Iterator<Object> it = internalQueue.iterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return unmask(it.next());
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    @Override
    public int size() {
        return internalQueue.size();
    }

    /**
     * Inserts the specified element into this queue.
     *
     * <p>Unlike standard concurrent queues, this method accepts `null`.
     */
    @Override
    public boolean offer(E e) {
        return internalQueue.offer(mask(e));
    }

    @Override
    public E poll() {
        return unmask(internalQueue.poll());
    }

    @Override
    public E peek() {
        return unmask(internalQueue.peek());
    }

    @Override
    public boolean isEmpty() {
        return internalQueue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return internalQueue.contains(mask(o));
    }

    @Override
    public boolean remove(Object o) {
        return internalQueue.remove(mask(o));
    }

    @Override
    public void clear() {
        internalQueue.clear();
    }
}
