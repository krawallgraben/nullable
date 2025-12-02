package de.krawallgraben.nullable;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe list implementation that prioritizes robust iteration over iteration performance.
 *
 * <p>This list allows concurrent modifications during iteration. The iterator attempts to "recover"
 * its position if the underlying list changes, based on the value and occurrence count of the last
 * returned element.
 *
 * <p><strong>Performance Note:</strong> The iteration logic tries to be cheap if the position of
 * the last returned element is still valid (contains the same value). Otherwise, it scans to
 * recover the position. This avoids O(N^2) complexity in the common case.
 *
 * @param <T> the type of elements in this list
 */
public class RobustValueIteratorList<T> implements Iterable<T> {

    private final ArrayList<T> list = new ArrayList<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Global counter for modifications
    private volatile long modCount = 0;

    // --- High-Performance Write Operations (O(1)) ---

    public void add(T element) {
        rwLock.writeLock().lock();
        try {
            list.add(element);
            modCount++;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void remove(int index) {
        rwLock.writeLock().lock();
        try {
            list.remove(index);
            modCount++;
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

    // --- The intelligent iterator ---

    @Override
    public Iterator<T> iterator() {
        return new ValueTrackingIterator();
    }

    private class ValueTrackingIterator implements Iterator<T> {
        // Physical cursor (index for the next access)
        private int cursor = 0;

        // State for recovery
        private long expectedModCount;
        private T lastReturnedValue = null;
        private boolean hasStarted = false;

        ValueTrackingIterator() {
            expectedModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            rwLock.readLock().lock();
            try {
                // If changed: try to repair the position logically
                if (modCount != expectedModCount) {
                    checkAndRecoverPosition();
                }
                return cursor < list.size();
            } finally {
                rwLock.readLock().unlock();
            }
        }

        @Override
        public T next() {
            rwLock.readLock().lock();
            try {
                // 1. Check for change & Repair
                if (modCount != expectedModCount) {
                    checkAndRecoverPosition();
                }

                if (cursor >= list.size()) {
                    throw new NoSuchElementException();
                }

                // 2. Get value
                T currentValue = list.get(cursor);

                this.lastReturnedValue = currentValue;

                // 3. Advance cursor
                cursor++;
                hasStarted = true;

                return currentValue;

            } finally {
                rwLock.readLock().unlock();
            }
        }

        /** Called when modCount doesn't match. Tries to validate position or recover. */
        private void checkAndRecoverPosition() {
            if (!hasStarted) {
                // If not started yet, simply reset
                cursor = 0;
                expectedModCount = modCount;
                return;
            }

            // Optimization: If the value at the position where we expect the last returned value
            // is still the same, we assume we are good.
            // The last returned value was at 'cursor - 1'.
            int lastIndex = cursor - 1;
            if (lastIndex >= 0 && lastIndex < list.size()) {
                if (Objects.equals(list.get(lastIndex), lastReturnedValue)) {
                    // Optimization: We are likely still aligned.
                    expectedModCount = modCount;
                    return;
                }
            }

            // Fallback: We need to find where 'lastReturnedValue' went.
            // Since we don't track occurrence count anymore to save O(N^2) cost,
            // we do a best-effort recovery: find the first occurrence of the value.

            int foundAtIndex = -1;

            for (int i = 0; i < list.size(); i++) {
                if (Objects.equals(list.get(i), lastReturnedValue)) {
                    foundAtIndex = i;
                    // We take the first one we find. This is a behavior change from O(N^2) version
                    // but necessary for performance.
                    break;
                }
            }

            // Set new cursor: We want the element AFTER it next
            if (foundAtIndex != -1) {
                cursor = foundAtIndex + 1;
                expectedModCount = modCount; // Accept change
            } else {
                throw new ConcurrentModificationException(
                        "Element " + lastReturnedValue + " was deleted. Recovery impossible.");
            }
        }
    }
}
