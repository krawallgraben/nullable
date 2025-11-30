package de.krawallgraben.nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FailFastThreadSafeList<T> implements Iterable<T> {

    // Allows null, fast get(i), fast add()
    private final List<T> list = new ArrayList<>();

    // ReadWriteLock allows parallel reads via get(i)
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void add(T element) {
        rwLock.writeLock().lock();
        try {
            list.add(element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void remove(int index) {
        rwLock.writeLock().lock();
        try {
            list.remove(index);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public T get(int index) {
        rwLock.readLock().lock();
        try {
            return list.get(index);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int size() {
        rwLock.readLock().lock();
        try {
            return list.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /** Returns the raw fail-fast iterator. NO locking here! */
    @Override
    public Iterator<T> iterator() {
        // We return the direct ArrayList iterator.
        // This checks on every next() if 'modCount' was changed.
        return list.iterator();
    }
}
