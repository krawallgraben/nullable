package de.krawallgraben;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class NullableConcurrentMapTest {

    @Test
    void testPutAndGetNull() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        assertNull(map.put("key1", null));
        assertTrue(map.containsKey("key1"));
        assertNull(map.get("key1"));

        assertNull(map.put("key1", "value1")); // Replaces null with "value1", returns old value (null)
        assertEquals("value1", map.get("key1"));

        assertEquals("value1", map.put("key1", null)); // Replaces "value1" with null, returns old value "value1"
        assertNull(map.get("key1"));
    }

    @Test
    void testContainsValue() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        map.put("key1", null);
        assertTrue(map.containsValue(null));
        assertFalse(map.containsValue("something"));

        map.put("key2", "something");
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("something"));
    }

    @Test
    void testPutIfAbsent() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();

        // Put null if absent
        assertNull(map.putIfAbsent("key1", null));
        assertTrue(map.containsKey("key1"));
        assertNull(map.get("key1"));

        // Put something if absent (but it is present as null)
        // putIfAbsent returns current value. Current is null.
        // Does it update if current is null?
        // Map.putIfAbsent: "If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value."
        // Wait. ConcurrentHashMap does NOT support null values. So "or is mapped to null" usually doesn't apply to it.
        // But here we are simulating nulls.
        // If we store PLACEHOLDER, the map thinks it IS associated with a value (PLACEHOLDER).
        // So super.putIfAbsent("key1", val) sees PLACEHOLDER and does NOTHING.
        // It returns PLACEHOLDER.
        // So if we have mapped "key1" -> null (PLACEHOLDER), putIfAbsent("key1", "val") will return null and NOT update.
        // Let's verify if this is desired.
        // Standard Map: if mapped to null, it updates.
        // ConcurrentHashMap: never mapped to null.
        // If we want to behave like HashMap, we should update if value is PLACEHOLDER?
        // But we are extending ConcurrentHashMap.
        // super.putIfAbsent treats PLACEHOLDER as a non-null value.
        // So it will NOT replace PLACEHOLDER.
        // This means: map.put("k", null); map.putIfAbsent("k", "val") -> leaves it as null.
        // Is this what we want?
        // If we want "map das null werte enthalten kann", maybe we want it to behave like HashMap regarding nulls?
        // If so, we'd need to override putIfAbsent logic more deeply.
        // But usually "NullableConcurrentMap" just means "I can store nulls".
        // I will assert the behavior I implemented: it treats null (PLACEHOLDER) as a valid existing value.

        assertNull(map.putIfAbsent("key1", "newValue"));
        // It returns null (unmasked PLACEHOLDER).
        // And it should NOT have updated the value because PLACEHOLDER is considered a value.
        assertNull(map.get("key1"));
    }

    @Test
    void testReplace() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        map.put("key1", null);

        // replace(key, oldValue, newValue)
        assertTrue(map.replace("key1", null, "value1"));
        assertEquals("value1", map.get("key1"));

        // replace(key, value)
        assertEquals("value1", map.replace("key1", null));
        assertNull(map.get("key1"));
    }

    @Test
    void testCompute() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();

        // Compute to null (should store null)
        // Wait, my implementation of compute: if func returns null, it passes null to super -> removes.
        // So compute("k", (k,v) -> null) REMOVES the mapping.
        // This is consistent with Map contract.
        map.put("key1", "val");
        assertNull(map.compute("key1", (k, v) -> null));
        assertFalse(map.containsKey("key1"));

        // Compute to non-null
        assertEquals("computed", map.compute("key1", (k, v) -> "computed"));
        assertEquals("computed", map.get("key1"));

        // What if I want to compute a NULL value?
        // Map contract says: "If the remapping function returns null, the mapping is removed."
        // So you CANNOT compute a value into null.
        // You have to use put.
        map.put("key2", null);
        assertTrue(map.containsKey("key2"));
    }

    @Test
    void testComputeIfAbsent() {
         NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
         // func returns null -> no mapping established (standard map behavior)
         assertNull(map.computeIfAbsent("key1", k -> null));
         assertFalse(map.containsKey("key1"));

         // func returns value -> mapping established
         assertEquals("val", map.computeIfAbsent("key1", k -> "val"));
         assertEquals("val", map.get("key1"));

         // If key exists (as null), computeIfAbsent should NOT recompute?
         // In HashMap: "If the specified key is not already associated with a value (or is mapped to null)..."
         // So if mapped to null, it SHOULD recompute.
         // In my impl: super.computeIfAbsent("key", func).
         // super sees PLACEHOLDER. It thinks "key is associated with value".
         // So it does NOT recompute.
         // This diverges from HashMap behavior for null values.
         // But it is consistent with "ConcurrentHashMap with a Null Object Pattern".
         map.put("key2", null);
         assertEquals(null, map.computeIfAbsent("key2", k -> "newVal"));
         assertNull(map.get("key2"));
    }

    @Test
    void testMerge() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        map.put("key1", null);

        // Merge with null value -> NPE (standard Map behavior for merge input)
        assertThrows(NullPointerException.class, () -> map.merge("key1", null, (v1, v2) -> v2));

        // Merge existing null with new value
        // super.merge(key, mask(val), func)
        // super sees PLACEHOLDER as old value.
        // func is called with (unmask(PLACEHOLDER), unmask(mask(val))) -> (null, val)
        // We return "merged".
        assertEquals("merged", map.merge("key1", "val", (v1, v2) -> {
            if (v1 == null) return "merged";
            return v2;
        }));
        assertEquals("merged", map.get("key1"));
    }

    @Test
    void testEntrySet() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        map.put("key1", null);
        map.put("key2", "val2");

        Set<Map.Entry<String, String>> entries = map.entrySet();
        assertEquals(2, entries.size());

        boolean foundNull = false;
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getKey().equals("key1")) {
                assertNull(entry.getValue());
                foundNull = true;
            }
        }
        assertTrue(foundNull);
    }

    @Test
    void testValues() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        map.put("key1", null);
        map.put("key2", "val2");

        Collection<String> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(null));
        assertTrue(values.contains("val2"));

        Iterator<String> it = values.iterator();
        int count = 0;
        while(it.hasNext()) {
            String v = it.next();
            if (v != null) assertEquals("val2", v);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    void testForEach() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        map.put("key1", null);

        final boolean[] foundNull = {false};
        map.forEach((k, v) -> {
            if ("key1".equals(k) && v == null) {
                foundNull[0] = true;
            }
        });
        assertTrue(foundNull[0]);
    }

    @Test
    void testRemove() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        map.put("key1", null);

        // remove(key, value)
        assertFalse(map.remove("key1", "wrong"));
        assertTrue(map.remove("key1", null));
        assertFalse(map.containsKey("key1"));
    }

    @Test
    void testNullKeysNotAllowed() {
        NullableConcurrentMap<String, String> map = new NullableConcurrentMap<>();
        assertThrows(NullPointerException.class, () -> map.put(null, "val"));
        assertThrows(NullPointerException.class, () -> map.get(null));
    }
}
