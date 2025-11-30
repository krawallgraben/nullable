package de.krawallgraben.nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A thread-safe deque that allows `null` elements.
 *
 * <p>This implementation wraps a {@link ConcurrentLinkedDeque} to support `null` values, which are
 * typically not allowed in standard concurrent collections.
 *
 * @param <E> the type of elements held in this deque
 */
@SuppressWarnings("serial")
public class NullableConcurrentDeque<E> implements Deque<E>, Serializable {

    private final ConcurrentLinkedDeque<Object> internalDeque;

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

    public NullableConcurrentDeque() {
        this.internalDeque = new ConcurrentLinkedDeque<>();
    }

    public NullableConcurrentDeque(Collection<? extends E> c) {
        this.internalDeque = new ConcurrentLinkedDeque<>();
        if (c != null) {
            for (E e : c) {
                add(e);
            }
        }
    }

    // Deque Methods

    /**
     * Inserts the specified element at the front of this deque.
     *
     * <p>Unlike standard concurrent deques, this method accepts `null`.
     */
    @Override
    public void addFirst(E e) {
        internalDeque.addFirst(mask(e));
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>Unlike standard concurrent deques, this method accepts `null`.
     */
    @Override
    public void addLast(E e) {
        internalDeque.addLast(mask(e));
    }

    /**
     * Inserts the specified element at the front of this deque.
     *
     * <p>Unlike standard concurrent deques, this method accepts `null`.
     */
    @Override
    public boolean offerFirst(E e) {
        return internalDeque.offerFirst(mask(e));
    }

    /**
     * Inserts the specified element at the end of this deque.
     *
     * <p>Unlike standard concurrent deques, this method accepts `null`.
     */
    @Override
    public boolean offerLast(E e) {
        return internalDeque.offerLast(mask(e));
    }

    @Override
    public E removeFirst() {
        return unmask(internalDeque.removeFirst());
    }

    @Override
    public E removeLast() {
        return unmask(internalDeque.removeLast());
    }

    @Override
    public E pollFirst() {
        return unmask(internalDeque.pollFirst());
    }

    @Override
    public E pollLast() {
        return unmask(internalDeque.pollLast());
    }

    @Override
    public E getFirst() {
        return unmask(internalDeque.getFirst());
    }

    @Override
    public E getLast() {
        return unmask(internalDeque.getLast());
    }

    @Override
    public E peekFirst() {
        return unmask(internalDeque.peekFirst());
    }

    @Override
    public E peekLast() {
        return unmask(internalDeque.peekLast());
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return internalDeque.removeFirstOccurrence(mask(o));
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return internalDeque.removeLastOccurrence(mask(o));
    }

    // Queue Methods

    /**
     * Inserts the specified element into the queue represented by this deque.
     *
     * <p>Unlike standard concurrent queues, this method accepts `null`.
     */
    @Override
    public boolean add(E e) {
        return internalDeque.add(mask(e));
    }

    /**
     * Inserts the specified element into the queue represented by this deque.
     *
     * <p>Unlike standard concurrent queues, this method accepts `null`.
     */
    @Override
    public boolean offer(E e) {
        return internalDeque.offer(mask(e));
    }

    @Override
    public E remove() {
        return unmask(internalDeque.remove());
    }

    @Override
    public E poll() {
        return unmask(internalDeque.poll());
    }

    @Override
    public E element() {
        return unmask(internalDeque.element());
    }

    @Override
    public E peek() {
        return unmask(internalDeque.peek());
    }

    // Stack Methods

    /**
     * Pushes an element onto the stack represented by this deque.
     *
     * <p>Unlike standard concurrent stacks, this method accepts `null`.
     */
    @Override
    public void push(E e) {
        internalDeque.push(mask(e));
    }

    @Override
    public E pop() {
        return unmask(internalDeque.pop());
    }

    // Collection Methods

    @Override
    public boolean remove(Object o) {
        return internalDeque.remove(mask(o));
    }

    @Override
    public boolean contains(Object o) {
        return internalDeque.contains(mask(o));
    }

    @Override
    public int size() {
        return internalDeque.size();
    }

    @Override
    public Iterator<E> iterator() {
        Iterator<Object> it = internalDeque.iterator();
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
    public Iterator<E> descendingIterator() {
        Iterator<Object> it = internalDeque.descendingIterator();
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
    public boolean isEmpty() {
        return internalDeque.isEmpty();
    }

    @Override
    public Object[] toArray() {
        Object[] internalArray = internalDeque.toArray();
        Object[] result = new Object[internalArray.length];
        for (int i = 0; i < internalArray.length; i++) {
            result[i] = unmask(internalArray[i]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        Object[] items = toArray();
        if (a.length < items.length) {
            return (T[]) java.util.Arrays.copyOf(items, items.length, a.getClass());
        }
        System.arraycopy(items, 0, a, 0, items.length);
        if (a.length > items.length) {
            a[items.length] = null;
        }
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c) {
            if (add(e)) modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            while (remove(e)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {
        internalDeque.clear();
    }
}
