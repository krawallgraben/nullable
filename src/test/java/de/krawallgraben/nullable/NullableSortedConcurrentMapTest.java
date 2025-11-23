package de.krawallgraben.nullable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NullableSortedConcurrentMapTest {

    @Test
    void testNaturalOrdering() {
        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>();
        map.put("b", "valB");
        map.put(null, "valNull");
        map.put("a", "valA");

        assertEquals(3, map.size());

        // Verify first key is null
        assertNull(map.firstKey());

        // Verify iteration order: null, a, b
        Iterator<String> it = map.keySet().iterator();
        assertNull(it.next());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
    }

    @Test
    void testNullValues() {
        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>();
        map.put("key1", null);
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsValue(null));
        assertNull(map.get("key1"));
    }

    @Test
    void testCustomComparator() {
        // Reverse order comparator
        Comparator<String> reverse = Comparator.reverseOrder();
        NullableSortedConcurrentMap<String, String> map =
                new NullableSortedConcurrentMap<>(reverse);

        map.put("a", "valA");
        map.put(null, "valNull");
        map.put("b", "valB");

        // Order should be: null (always first per requirement), then b, then a
        // Wait. Requirement: "null werte am anfang einsortiert sein sollen"
        // My implementation forces null to be less than everything (return -1).
        // Even if delegate comparator is reverse order?
        // Code:
        // if (o1 == PLACEHOLDER) return o2 == PLACEHOLDER ? 0 : -1;
        // So null is ALWAYS first, regardless of delegate.
        // This satisfies "null werte am anfang".

        Iterator<String> it = map.keySet().iterator();
        assertNull(it.next());
        assertEquals("b", it.next());
        assertEquals("a", it.next());
    }

    @Test
    void testPollFirstEntry() {
        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>();
        map.put("a", "valA");
        map.put(null, "valNull");

        Map.Entry<String, String> entry = map.pollFirstEntry();
        assertNotNull(entry);
        assertNull(entry.getKey());
        assertEquals("valNull", entry.getValue());

        assertEquals(1, map.size());
        assertEquals("a", map.firstKey());
    }

    @Test
    void testKeySetNavigable() {
        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>();
        map.put("1", "one");
        map.put("3", "three");
        map.put(null, "null");

        NavigableSet<String> keys = map.keySet();
        assertEquals("1", keys.higher(null));
        assertNull(keys.lower("1"));
        assertNull(keys.floor(null)); // floor(null) -> null
        assertEquals("null", map.get(keys.floor(null)));
    }

    @Test
    void testEntrySet() {
        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>();
        map.put(null, "valNull");
        map.put("a", "valA");

        Set<Map.Entry<String, String>> entries = map.entrySet();
        assertEquals(2, entries.size());
        for (Map.Entry<String, String> e : entries) {
            if (e.getKey() == null) assertEquals("valNull", e.getValue());
            else assertEquals("valA", e.getValue());
        }
    }

    @Test
    void testSubMap() {
        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>();
        map.put(null, "nullVal");
        map.put("a", "aVal");
        map.put("c", "cVal");

        // HeadMap
        Map<String, String> head = map.headMap("c");
        assertEquals(2, head.size());
        assertTrue(head.containsKey(null));
        assertTrue(head.containsKey("a"));
        assertFalse(head.containsKey("c"));

        // TailMap
        Map<String, String> tail = map.tailMap("a");
        assertEquals(2, tail.size());
        assertTrue(tail.containsKey("a"));
        assertTrue(tail.containsKey("c"));
        assertFalse(tail.containsKey(null));

        // SubMap
        Map<String, String> sub = map.subMap(null, "c"); // from null (inclusive) to c (exclusive)
        assertEquals(2, sub.size());
        assertTrue(sub.containsKey(null));
        assertTrue(sub.containsKey("a"));

        // Verify views are wrapped correctly (not exposing placeholders)
        assertNull(head.keySet().iterator().next()); // First key should be null
    }
}
