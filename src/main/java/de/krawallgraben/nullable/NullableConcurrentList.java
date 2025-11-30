package de.krawallgraben.nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A thread-safe list that allows `null` elements.
 *
 * <p>This implementation delegates to a {@link CopyOnWriteArrayList}. Since {@code
 * CopyOnWriteArrayList} already supports {@code null} elements natively, this class primarily
 * serves to provide a consistent naming convention within the {@code Nullable*} package family.
 *
 * <p><strong>Note on Concurrency:</strong> While this list is thread-safe, write operations (add,
 * set, remove) are expensive as they involve copying the entire underlying array. This is
 * inherently <em>blocking</em> (locks are used during the copy). Read operations are non-blocking
 * and very fast. This fits best for read-heavy workloads.
 *
 * @param <E> the type of elements held in this list
 */
@SuppressWarnings("serial")
public class NullableConcurrentList<E> implements List<E>, RandomAccess, Serializable {

    private final CopyOnWriteArrayList<E> internalList;

    public NullableConcurrentList() {
        this.internalList = new CopyOnWriteArrayList<>();
    }

    public NullableConcurrentList(Collection<? extends E> c) {
        this.internalList = new CopyOnWriteArrayList<>(c);
    }

    public NullableConcurrentList(E[] toCopyIn) {
        this.internalList = new CopyOnWriteArrayList<>(toCopyIn);
    }

    // Delegation Methods

    @Override
    public int size() {
        return internalList.size();
    }

    @Override
    public boolean isEmpty() {
        return internalList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return internalList.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return internalList.iterator();
    }

    @Override
    public Object[] toArray() {
        return internalList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return internalList.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return internalList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return internalList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return internalList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return internalList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return internalList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return internalList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return internalList.retainAll(c);
    }

    @Override
    public void clear() {
        internalList.clear();
    }

    @Override
    public boolean equals(Object o) {
        return internalList.equals(o);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public E get(int index) {
        return internalList.get(index);
    }

    @Override
    public E set(int index, E element) {
        return internalList.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        internalList.add(index, element);
    }

    @Override
    public E remove(int index) {
        return internalList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return internalList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return internalList.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return internalList.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return internalList.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return internalList.subList(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        internalList.forEach(action);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return internalList.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        internalList.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        internalList.sort(c);
    }

    @Override
    public Spliterator<E> spliterator() {
        return internalList.spliterator();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }
}
