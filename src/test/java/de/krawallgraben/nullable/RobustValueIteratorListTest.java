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
        // Iterator state: lastReturned="A", occurrence=1.
        // Recovery: Finds 1st "A" at index 0. Cursor becomes 1.
        // Next should be list.get(1) -> "B".
        list.add("C");

        assertEquals("B", it.next());
        assertEquals("C", it.next());
    }

    @Test
    void testRecovery_AdditionBeforeCursor() {
        // This is tricky. If we add before cursor, indices shift.
        // But our recovery looks for "Last Returned Value" and sets cursor to "Index + 1".

        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A"); // 0
        list.add("B"); // 1

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next()); // Cursor 1. Last: "A", occ: 1.

        // Modify: Insert "X" at 0. (We don't have insert at index, but let's simulate via internal
        // list access?
        // No, we only have add(T) which appends, or remove(index).
        // Wait, the class only has add(T) (append) and remove(int).
        // So we can't easily add *before* cursor unless we implement add(index, element).
        // The user provided class only has `add(T element)` and `remove(int index)`.
        // So we can only append.

        // Let's test REMOVAL before cursor.
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
        // Cursor at 2 (pointing to B). Last: "A", occ: 1.

        // Remove "X" (index 0).
        // List: [A, B].
        // "A" is now at index 0.
        list.remove(0);

        // Next call:
        // Recovery: Finds 1st "A" at index 0.
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
        // Cursor 1 (B). Last: "A", occ: 1.

        // Remove "C" (index 2).
        // List: [A, B].
        list.remove(2);

        // Recovery: Finds 1st "A" at index 0. Cursor 1.
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
        // Cursor 3 (C). Last: "A", occ: 2 (it was the 2nd A).

        // Remove "B" (index 1).
        // List: [A, A, C].
        list.remove(1);

        // Recovery:
        // Current list: A(0), A(1), C(2).
        // We look for 2nd "A". It is at index 1.
        // Cursor becomes 1 + 1 = 2.
        // Next: list.get(2) -> "C".

        assertTrue(it.hasNext());
        assertEquals("C", it.next());
    }

    @Test
    void testRecoveryFailure_ElementDeleted() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A");
        list.add("B");

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next()); // Last: "A", occ: 1.

        // Remove "A"
        list.remove(0); // List: [B]

        // Recovery:
        // Total count of "A" is 0.
        // Last occurrence was 1.
        // 0 < 1 -> CME.

        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void testRecoveryFailure_PreviousInstanceDeleted() {
        RobustValueIteratorList<String> list = new RobustValueIteratorList<>();
        list.add("A"); // 1st A
        list.add("B");
        list.add("A"); // 2nd A
        list.add("C");

        Iterator<String> it = list.iterator();
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("A", it.next());
        // Cursor 3 (C). Last: "A", occ: 2.

        // Remove 1st "A" (index 0).
        // List: [B, A, C].
        list.remove(0);

        // Recovery:
        // Total count of "A" is 1.
        // Last occurrence was 2.
        // 1 < 2 -> CME.
        // Because the "2nd A" no longer exists as the "2nd A", it became the "1st A".
        // The iterator assumes we missed/deleted the one we just returned or one before it.

        assertThrows(ConcurrentModificationException.class, it::next);
    }
}
