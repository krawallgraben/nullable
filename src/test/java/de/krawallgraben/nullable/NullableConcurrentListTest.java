package de.krawallgraben.nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import org.junit.jupiter.api.Test;

class NullableConcurrentListTest {

    @Test
    void testAddGetNull() {
        NullableConcurrentList<String> list = new NullableConcurrentList<>();
        list.add("A");
        list.add(null);
        list.add("B");

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertNull(list.get(1));
        assertEquals("B", list.get(2));
    }

    @Test
    void testSetNull() {
        NullableConcurrentList<String> list = new NullableConcurrentList<>();
        list.add("A");
        list.set(0, null);
        assertNull(list.get(0));
    }

    @Test
    void testIndexOfNull() {
        NullableConcurrentList<String> list = new NullableConcurrentList<>();
        list.add("A");
        list.add(null);

        assertEquals(1, list.indexOf(null));
        assertFalse(list.contains("Z"));
        assertTrue(list.contains(null));
    }

    @Test
    void testIterator() {
        NullableConcurrentList<String> list = new NullableConcurrentList<>();
        list.add("A");
        list.add(null);

        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertNull(it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testRemove() {
        NullableConcurrentList<String> list = new NullableConcurrentList<>();
        list.add("A");
        list.add(null);
        list.add("B");

        assertTrue(list.remove(null));
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }
}
