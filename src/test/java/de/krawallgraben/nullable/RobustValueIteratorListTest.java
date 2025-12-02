package de.krawallgraben.nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class RobustValueIteratorListTest {

    @Test
    void testAddGetSize() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A");
        list.add(null);
        list.add("B");

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertNull(list.get(1));
        assertEquals("B", list.get(2));
    }

    @Test
    void testBasicIterator() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("one");
        list.add("two");

        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertEquals("two", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testRecovery_AdditionAfterCursor() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A");
        list.add("B");

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next()); // Cursor at 1 (pointing to B)

        // Modify list: Add "C" at the end.
        // List: [A, B, C]
        // Iterator state: lastReturned="A".
        // Recovery check: list.get(0) == "A". Match.
        // Should continue.
        list.add("C");

        assertEquals("B", it.next());
        assertEquals("C", it.next());
    }

    @Test
    void testRecovery_RemovalBeforeCursor() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("X");
        list.add("A");
        list.add("B");

        Iterator<String> it = list.iterator();
        assertEquals("X", it.next());
        assertEquals("A", it.next());
        // Cursor at 2 (pointing to B). Last: "A".

        // Remove "X" (index 0).
        // List: [A, B].
        // "A" is now at index 0.
        list.remove(0);

        // Next call:
        // Recovery check: list.get(1) -> "B". Last: "A". Mismatch.
        // Recovery scan: Find "A". Found at 0.
        // Cursor becomes 0 + 1 = 1.
        // Next is list.get(1) -> "B". Correct.

        assertEquals("B", it.next());
    }

    @Test
    void testRecovery_RemovalAfterCursor() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next());
        // Cursor 1 (B). Last: "A".

        // Remove "C" (index 2).
        // List: [A, B].
        list.remove(2);

        // Recovery check: list.get(0) -> "A". Match.
        // Continue.
        // Next: list.get(1) -> "B".
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testRecovery_DuplicateValues() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A"); // 1st A
        list.add("B");
        list.add("A"); // 2nd A
        list.add("C");

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("A", it.next());
        // Cursor 3 (C). Last: "A" (the 2nd one).

        // Remove "B" (index 1).
        // List: [A, A, C].
        list.remove(1);

        // Recovery:
        // Check: list.get(2) -> "C". Last "A". Mismatch.
        // Recovery Scan: Find "A".
        // Finds first "A" at index 0.
        // Cursor becomes 0 + 1 = 1.
        // Next: list.get(1) -> "A" (The second A).

        // Note: The behavior changed from seeing "C" to seeing "A" then "C".
        // This is due to losing "occurrence count" tracking for performance.

        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("C", it.next());
    }

    @Test
    void testRecoveryFailure_ElementDeleted() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A");
        list.add("B");

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next()); // Last: "A".

        // Remove "A"
        list.remove(0); // List: [B]

        // Recovery:
        // Check: list.get(0) -> "B". Last "A". Mismatch.
        // Scan: Find "A". Not found.
        // CME.

        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void testRecovery_PreviousInstanceDeleted_SurvivableNow() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A"); // 1st A
        list.add("B");
        list.add("A"); // 2nd A
        list.add("C");

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("A", it.next());
        // Cursor 3 (C). Last: "A".

        // Remove 1st "A" (index 0).
        // List: [B, A, C].
        list.remove(0);

        // Recovery:
        // Check: list.get(2) -> "C". Last "A". Mismatch.
        // Scan: Find "A". Found at index 1.
        // Cursor becomes 1 + 1 = 2.
        // Next: list.get(2) -> "C".

        // Previously this threw CME because occurrence count mismatch.
        // Now it survives because we just look for *any* A.
        // It correctly finds the remaining A and continues.

        assertEquals("C", it.next());
    }
}
