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
 * <p><strong>Performance Note:</strong> While read/write operations (get, add, remove) are fast
 * (O(1) or O(N) for remove), the iteration is expensive (O(N^2)) because it recalculates position
 * context on every step. Use this only when iteration robustness is more important than speed.
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

    // --- The intelligent, "expensive" Iterator ---

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
        private int lastValueOccurrence = 0; // "Which occurrence of 'A' was it?"
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
                    recoverPosition();
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
                    recoverPosition();
                }

                if (cursor >= list.size()) {
                    throw new NoSuchElementException();
                }

                // 2. Get value
                T currentValue = list.get(cursor);

                // 3. Calculate metadata for next time (The "expensive" part)
                // We need to know: Which occurrence of this value is it up to here?
                // This costs O(cursor).
                calculateOccurrenceStats(currentValue, cursor);

                // 4. Advance cursor
                cursor++;
                hasStarted = true;

                return currentValue;

            } finally {
                rwLock.readLock().unlock();
            }
        }

        /** Calculates which duplicate we currently have. */
        private void calculateOccurrenceStats(T value, int currentIndex) {
            int occurrence = 0;
            // Scan from 0 to current index
            for (int i = 0; i <= currentIndex; i++) {
                if (Objects.equals(list.get(i), value)) {
                    occurrence++;
                }
            }
            this.lastReturnedValue = value;
            this.lastValueOccurrence = occurrence;
        }

        /**
         * Called when modCount doesn't match. Tries to reset cursor based on "Value + n-th
         * repetition".
         */
        private void recoverPosition() {
            if (!hasStarted) {
                // If not started yet, simply reset
                cursor = 0;
                expectedModCount = modCount;
                return;
            }

            // A. Count how often the value exists NOW in the list
            int totalCountNow = 0;
            for (T item : list) {
                if (Objects.equals(item, lastReturnedValue)) {
                    totalCountNow++;
                }
            }

            // B. "If it occurs less often, it is broken"
            if (totalCountNow < lastValueOccurrence) {
                throw new ConcurrentModificationException(
                        "Element "
                                + lastReturnedValue
                                + " was deleted (or instances before it). Recovery impossible.");
            }

            // C. Optimistic assumption: We look for the n-th occurrence again
            int foundAtIndex = -1;
            int currentOccurrence = 0;

            for (int i = 0; i < list.size(); i++) {
                if (Objects.equals(list.get(i), lastReturnedValue)) {
                    currentOccurrence++;
                    if (currentOccurrence == lastValueOccurrence) {
                        foundAtIndex = i;
                        break;
                    }
                }
            }

            // D. Set new cursor: We want the element AFTER it next
            if (foundAtIndex != -1) {
                cursor = foundAtIndex + 1;
                expectedModCount = modCount; // Accept change
            } else {
                // Should theoretically be caught by Check B, but for safety:
                throw new ConcurrentModificationException("Critical error during recovery.");
            }
        }
    }
}
