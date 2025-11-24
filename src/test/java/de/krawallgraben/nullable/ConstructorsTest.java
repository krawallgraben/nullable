package de.krawallgraben.nullable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class ConstructorsTest {

    @Test
    void testNullableConcurrentMapDefaultConstructor() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    void testNullableConcurrentMapIntConstructor() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>(32);
        assertTrue(map.isEmpty());
        // Can't easily check internal capacity without reflection, but we check instantiation.
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    void testNullableConcurrentMapMapConstructor() {
        Map<String, String> source = new HashMap<>();
        source.put("k1", "v1");
        source.put("k2", null);
        source.put(null, "v3");

        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>(source);
        assertEquals(3, map.size());
        assertEquals("v1", map.get("k1"));
        assertNull(map.get("k2"));
        assertEquals("v3", map.get(null));
    }

    @Test
    void testNullableConcurrentMapLoadFactorConstructor() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>(16, 0.75f);
        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    void testNullableConcurrentMapConcurrencyLevelConstructor() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>(16, 0.75f, 4);
        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    void testNullableSortedConcurrentMapDefaultConstructor() {
        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>();
        assertTrue(map.isEmpty());
        map.put("key", "value");
        assertEquals("value", map.get("key"));
    }

    @Test
    void testNullableSortedConcurrentMapComparatorConstructor() {
        NullableSortedConcurrentMap<String, String> map =
                new NullableSortedConcurrentMap<>(Comparator.reverseOrder());
        assertTrue(map.isEmpty());
        map.put("a", "valA");
        map.put("b", "valB");
        assertEquals("b", map.firstKey()); // Reverse order
    }

    @Test
    void testNullableSortedConcurrentMapMapConstructor() {
        Map<String, String> source = new HashMap<>();
        source.put("b", "v1");
        source.put("a", null);
        source.put(null, "v3");

        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>(source);
        assertEquals(3, map.size());
        assertEquals("v1", map.get("b"));
        assertNull(map.get("a"));
        assertEquals("v3", map.get(null));
        // Check order
        assertNull(map.firstKey());
        assertEquals("b", map.lastKey());
    }

    @Test
    void testNullableSortedConcurrentMapSortedMapConstructor() {
        // Source sorted map with reverse order
        TreeMap<String, String> source = new TreeMap<>(Comparator.reverseOrder());
        source.put("a", "v1");
        source.put("b", "v2");
        // TreeMap doesn't support null keys usually, so we don't put null key in source for this
        // test, unless we use a special comparator. Standard TreeMap throws NPE on null key.

        NullableSortedConcurrentMap<String, String> map = new NullableSortedConcurrentMap<>(source);
        assertEquals(2, map.size());
        assertEquals("b", map.firstKey()); // Reverse order: b comes before a
        assertEquals("a", map.lastKey());

        // But our map supports nulls, so we can add one now
        map.put(null, "nullVal");
        // Null is always first in our implementation
        assertNull(map.firstKey());
    }
}
