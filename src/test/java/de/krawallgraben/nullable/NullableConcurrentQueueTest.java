package de.krawallgraben.nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import org.junit.jupiter.api.Test;

class NullableConcurrentQueueTest {

    @Test
    void testOfferPollNull() {
        NullableConcurrentQueue<String> queue = new NullableConcurrentQueue<>();
        assertTrue(queue.offer(null));
        assertTrue(queue.offer("test"));
        assertTrue(queue.offer(null));

        assertEquals(3, queue.size());
        assertNull(queue.poll());
        assertEquals("test", queue.poll());
        assertNull(queue.poll());
        assertNull(queue.poll()); // Empty now
    }

    @Test
    void testPeek() {
        NullableConcurrentQueue<Integer> queue = new NullableConcurrentQueue<>();
        assertNull(queue.peek());
        queue.add(1);
        assertEquals(1, queue.peek());
        queue.add(null);
        queue.poll();
        assertNull(queue.peek()); // Should be the null element
        queue.poll();
        assertNull(queue.peek()); // Empty
    }

    @Test
    void testIterator() {
        NullableConcurrentQueue<String> queue = new NullableConcurrentQueue<>();
        queue.add("A");
        queue.add(null);
        queue.add("B");

        Iterator<String> it = queue.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertNull(it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testRemove() {
        NullableConcurrentQueue<String> queue = new NullableConcurrentQueue<>();
        queue.add("A");
        queue.add(null);
        queue.add("B");

        assertTrue(queue.remove(null));
        assertEquals(2, queue.size());
        assertEquals("A", queue.peek());
        // "A" and "B" remain

        Iterator<String> it = queue.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
    }
}
