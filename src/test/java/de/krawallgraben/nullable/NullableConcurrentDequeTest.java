package de.krawallgraben.nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import org.junit.jupiter.api.Test;

class NullableConcurrentDequeTest {

    @Test
    void testAddPollFirstLastNull() {
        NullableConcurrentDeque<String> deque = new NullableConcurrentDeque<>();
        deque.addFirst(null);
        deque.addLast("last");
        deque.addFirst("first");
        deque.addLast(null);

        // Order: first, null, last, null
        assertEquals(4, deque.size());
        assertEquals("first", deque.pollFirst());
        assertNull(deque.pollFirst());
        assertEquals("last", deque.pollFirst());
        assertNull(deque.pollFirst());
        assertNull(deque.pollFirst());
    }

    @Test
    void testPeekFirstLast() {
        NullableConcurrentDeque<Integer> deque = new NullableConcurrentDeque<>();
        assertNull(deque.peekFirst());
        assertNull(deque.peekLast());

        deque.add(null);
        // [null]
        assertNull(deque.peekFirst());
        assertNull(deque.peekLast());

        deque.addFirst(1);
        // [1, null]
        assertEquals(1, deque.peekFirst());
        assertNull(deque.peekLast());
    }

    @Test
    void testDescendingIterator() {
        NullableConcurrentDeque<String> deque = new NullableConcurrentDeque<>();
        deque.add("A");
        deque.add(null);
        deque.add("B");

        Iterator<String> it = deque.descendingIterator();
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertTrue(it.hasNext());
        assertNull(it.next());
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testRemoveOccurrence() {
        NullableConcurrentDeque<String> deque = new NullableConcurrentDeque<>();
        deque.add(null);
        deque.add("A");
        deque.add(null);

        assertTrue(deque.removeLastOccurrence(null));
        assertEquals(2, deque.size());
        // [null, "A"]

        Iterator<String> it = deque.iterator();
        assertNull(it.next());
        assertEquals("A", it.next());
    }
}
