package de.krawallgraben.nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FailFastThreadSafeListTest {

    @Test
    void testAddGetSize() {
        FailFastThreadSafeList<String> list = new FailFastThreadSafeList<>();
        list.add("A");
        list.add(null);
        list.add("B");

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertNull(list.get(1));
        assertEquals("B", list.get(2));
    }

    @Test
    void testRemove() {
        FailFastThreadSafeList<String> list = new FailFastThreadSafeList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        list.remove(1); // remove B

        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test
    void testIteratorBehavior() {
        FailFastThreadSafeList<String> list = new FailFastThreadSafeList<>();
        list.add("one");
        list.add("two");

        Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertEquals("two", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testFailFastIterator() {
        FailFastThreadSafeList<String> list = new FailFastThreadSafeList<>();
        list.add("A");
        Iterator<String> it = list.iterator();

        list.add("B"); // modification after iterator creation

        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void testConcurrentReadWrite() throws InterruptedException {
        FailFastThreadSafeList<Integer> list = new FailFastThreadSafeList<>();
        int threads = 10;
        int iterations = 1000;
        ExecutorService es = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            es.submit(
                    () -> {
                        try {
                            for (int j = 0; j < iterations; j++) {
                                list.add(j);
                                list.get(0); // read as well
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        es.shutdown();

        assertEquals(threads * iterations, list.size());
    }
}
