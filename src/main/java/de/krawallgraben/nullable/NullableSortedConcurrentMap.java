package de.krawallgraben.nullable;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A sorted, thread-safe map that allows `null` as keys and values.
 *
 * <p>This implementation wraps a {@link ConcurrentSkipListMap}. `null` keys are internally masked
 * and sorted to the beginning.
 *
 * <p>It behaves like a standard {@link ConcurrentNavigableMap} but supports nulls.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
@SuppressWarnings("serial")
public class NullableSortedConcurrentMap<K, V>
        implements ConcurrentNavigableMap<K, V>, Serializable {

    private final ConcurrentNavigableMap<Object, Object> internalMap;

    /** Placeholder for `null`. */
    private enum NullPlaceholder {
        INSTANCE;

        @Override
        public String toString() {
            return "null";
        }
    }

    private static Object mask(Object value) {
        return value == null ? NullPlaceholder.INSTANCE : value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmask(Object value) {
        return value == NullPlaceholder.INSTANCE ? null : (T) value;
    }

    // Comparator wrapper to handle NullPlaceholder
    private static class NullSafeComparator<K> implements Comparator<Object>, Serializable {
        private final Comparator<? super K> delegate;

        NullSafeComparator(Comparator<? super K> delegate) {
            this.delegate = delegate;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            if (o1 == NullPlaceholder.INSTANCE) {
                return o2 == NullPlaceholder.INSTANCE ? 0 : -1;
            }
            if (o2 == NullPlaceholder.INSTANCE) {
                return 1;
            }
            if (delegate != null) {
                return delegate.compare((K) o1, (K) o2);
            } else {
                return ((Comparable<? super K>) o1).compareTo((K) o2);
            }
        }
    }

    public NullableSortedConcurrentMap() {
        this.internalMap = new ConcurrentSkipListMap<>(new NullSafeComparator<>(null));
    }

    public NullableSortedConcurrentMap(Comparator<? super K> comparator) {
        this.internalMap = new ConcurrentSkipListMap<>(new NullSafeComparator<>(comparator));
    }

    public NullableSortedConcurrentMap(Map<? extends K, ? extends V> m) {
        this();
        this.putAll(m);
    }

    public NullableSortedConcurrentMap(SortedMap<K, ? extends V> m) {
        this(m.comparator());
        this.putAll(m);
    }

    // Private constructor for wrapping sub-maps
    private NullableSortedConcurrentMap(ConcurrentNavigableMap<Object, Object> internalMap) {
        this.internalMap = internalMap;
    }

    // ConcurrentNavigableMap methods

    @Override
    public ConcurrentNavigableMap<K, V> subMap(
            K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return new NullableSortedConcurrentMap<>(
                internalMap.subMap(mask(fromKey), fromInclusive, mask(toKey), toInclusive));
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return new NullableSortedConcurrentMap<>(internalMap.headMap(mask(toKey), inclusive));
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return new NullableSortedConcurrentMap<>(internalMap.tailMap(mask(fromKey), inclusive));
    }

    @Override
    public ConcurrentNavigableMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    @Override
    public ConcurrentNavigableMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    @Override
    public ConcurrentNavigableMap<K, V> descendingMap() {
        return new NullableSortedConcurrentMap<>(internalMap.descendingMap());
    }

    @Override
    public NavigableSet<K> navigableKeySet() {
        return new KeySetView(internalMap.navigableKeySet());
    }

    @Override
    public NavigableSet<K> keySet() {
        return navigableKeySet();
    }

    @Override
    public NavigableSet<K> descendingKeySet() {
        return new KeySetView(internalMap.descendingKeySet());
    }

    // Entry accessors

    @Override
    public Entry<K, V> ceilingEntry(K key) {
        return unmaskEntry(internalMap.ceilingEntry(mask(key)));
    }

    @Override
    public K ceilingKey(K key) {
        return unmask(internalMap.ceilingKey(mask(key)));
    }

    @Override
    public Entry<K, V> floorEntry(K key) {
        return unmaskEntry(internalMap.floorEntry(mask(key)));
    }

    @Override
    public K floorKey(K key) {
        return unmask(internalMap.floorKey(mask(key)));
    }

    @Override
    public Entry<K, V> higherEntry(K key) {
        return unmaskEntry(internalMap.higherEntry(mask(key)));
    }

    @Override
    public K higherKey(K key) {
        return unmask(internalMap.higherKey(mask(key)));
    }

    @Override
    public Entry<K, V> lowerEntry(K key) {
        return unmaskEntry(internalMap.lowerEntry(mask(key)));
    }

    @Override
    public K lowerKey(K key) {
        return unmask(internalMap.lowerKey(mask(key)));
    }

    @Override
    public Entry<K, V> firstEntry() {
        return unmaskEntry(internalMap.firstEntry());
    }

    @Override
    public Entry<K, V> lastEntry() {
        return unmaskEntry(internalMap.lastEntry());
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return unmaskEntry(internalMap.pollFirstEntry());
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return unmaskEntry(internalMap.pollLastEntry());
    }

    // Map basic methods

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(mask(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(mask(value));
    }

    @Override
    public V get(Object key) {
        return unmask(internalMap.get(mask(key)));
    }

    @Override
    public V put(K key, V value) {
        return unmask(internalMap.put(mask(key), mask(value)));
    }

    @Override
    public V remove(Object key) {
        return unmask(internalMap.remove(mask(key)));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    // ConcurrentMap methods

    @Override
    public V putIfAbsent(K key, V value) {
        return unmask(internalMap.putIfAbsent(mask(key), mask(value)));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return internalMap.remove(mask(key), mask(value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return internalMap.replace(mask(key), mask(oldValue), mask(newValue));
    }

    @Override
    public V replace(K key, V value) {
        return unmask(internalMap.replace(mask(key), mask(value)));
    }

    // SortedMap methods

    @Override
    @SuppressWarnings("unchecked")
    public Comparator<? super K> comparator() {
        Comparator<?> internal = internalMap.comparator();
        if (internal instanceof NullSafeComparator) {
            return ((NullSafeComparator<K>) internal).delegate;
        }
        return null; // Should not happen with our constructors
    }

    @Override
    public K firstKey() {
        return unmask(internalMap.firstKey());
    }

    @Override
    public K lastKey() {
        return unmask(internalMap.lastKey());
    }

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySetView();
    }

    // Additional Map defaults

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Object val = internalMap.get(mask(key));
        return val == null ? defaultValue : unmask(val);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        internalMap.forEach((k, v) -> action.accept(unmask(k), unmask(v)));
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        internalMap.replaceAll((k, v) -> mask(function.apply(unmask(k), unmask(v))));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return unmask(
                internalMap.computeIfAbsent(
                        mask(key),
                        k -> {
                            V result = mappingFunction.apply(unmask(k));
                            return result == null ? null : mask(result);
                        }));
    }

    @Override
    public V computeIfPresent(
            K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(
                internalMap.computeIfPresent(
                        mask(key),
                        (k, v) -> {
                            V result = remappingFunction.apply(unmask(k), unmask(v));
                            return result == null ? null : mask(result);
                        }));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(
                internalMap.compute(
                        mask(key),
                        (k, v) -> {
                            V result = remappingFunction.apply(unmask(k), unmask(v));
                            return result == null ? null : mask(result);
                        }));
    }

    @Override
    public V merge(
            K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return unmask(
                internalMap.compute(
                        mask(key),
                        (k, oldVal) -> {
                            V oldValue = unmask(oldVal);
                            V newValue;
                            if (oldValue == null) {
                                newValue = value;
                            } else {
                                newValue = remappingFunction.apply(oldValue, value);
                            }
                            return newValue == null ? null : mask(newValue);
                        }));
    }

    // Helper methods and classes

    private Entry<K, V> unmaskEntry(Entry<Object, Object> entry) {
        if (entry == null) return null;
        return new EntryWrapper(entry);
    }

    private class KeySetView extends AbstractSet<K> implements NavigableSet<K> {
        private final NavigableSet<Object> base;

        KeySetView(NavigableSet<Object> base) {
            this.base = base;
        }

        @Override
        public Iterator<K> iterator() {
            Iterator<Object> it = base.iterator();
            return new Iterator<K>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public K next() {
                    return unmask(it.next());
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public int size() {
            return base.size();
        }

        @Override
        public boolean contains(Object o) {
            return base.contains(mask(o));
        }

        @Override
        public boolean remove(Object o) {
            return base.remove(mask(o));
        }

        @Override
        public K lower(K k) {
            return unmask(base.lower(mask(k)));
        }

        @Override
        public K floor(K k) {
            return unmask(base.floor(mask(k)));
        }

        @Override
        public K ceiling(K k) {
            return unmask(base.ceiling(mask(k)));
        }

        @Override
        public K higher(K k) {
            return unmask(base.higher(mask(k)));
        }

        @Override
        public K pollFirst() {
            return unmask(base.pollFirst());
        }

        @Override
        public K pollLast() {
            return unmask(base.pollLast());
        }

        @Override
        public Comparator<? super K> comparator() {
            return NullableSortedConcurrentMap.this.comparator();
        }

        @Override
        public K first() {
            return unmask(base.first());
        }

        @Override
        public K last() {
            return unmask(base.last());
        }

        @Override
        public NavigableSet<K> descendingSet() {
            return new KeySetView(base.descendingSet());
        }

        @Override
        public Iterator<K> descendingIterator() {
            Iterator<Object> it = base.descendingIterator();
            return new Iterator<K>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public K next() {
                    return unmask(it.next());
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public NavigableSet<K> subSet(
                K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
            return new KeySetView(
                    base.subSet(mask(fromElement), fromInclusive, mask(toElement), toInclusive));
        }

        @Override
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return new KeySetView(base.headSet(mask(toElement), inclusive));
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return new KeySetView(base.tailSet(mask(fromElement), inclusive));
        }

        @Override
        public java.util.SortedSet<K> subSet(K fromElement, K toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        @Override
        public java.util.SortedSet<K> headSet(K toElement) {
            return headSet(toElement, false);
        }

        @Override
        public java.util.SortedSet<K> tailSet(K fromElement) {
            return tailSet(fromElement, true);
        }
    }

    private class ValuesView extends AbstractCollection<V> {
        private final Collection<Object> base = internalMap.values();

        @Override
        public Iterator<V> iterator() {
            Iterator<Object> it = base.iterator();
            return new Iterator<V>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public V next() {
                    return unmask(it.next());
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public int size() {
            return base.size();
        }

        @Override
        public boolean contains(Object o) {
            return base.contains(mask(o));
        }

        @Override
        public void clear() {
            base.clear();
        }
    }

    private class EntrySetView extends AbstractSet<Entry<K, V>> {
        private final Set<Entry<Object, Object>> base = internalMap.entrySet();

        @Override
        public Iterator<Entry<K, V>> iterator() {
            Iterator<Entry<Object, Object>> it = base.iterator();
            return new Iterator<Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<K, V> next() {
                    return new EntryWrapper(it.next());
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public int size() {
            return base.size();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            Object val = internalMap.get(mask(e.getKey()));
            return val != null && Objects.equals(val, mask(e.getValue()));
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return internalMap.remove(mask(e.getKey()), mask(e.getValue()));
        }

        @Override
        public void clear() {
            base.clear();
        }
    }

    private class EntryWrapper implements Entry<K, V> {
        private final Entry<Object, Object> entry;

        EntryWrapper(Entry<Object, Object> entry) {
            this.entry = entry;
        }

        @Override
        public K getKey() {
            return unmask(entry.getKey());
        }

        @Override
        public V getValue() {
            return unmask(entry.getValue());
        }

        @Override
        public V setValue(V value) {
            return unmask(entry.setValue(mask(value)));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
            return Objects.equals(getKey(), other.getKey())
                    && Objects.equals(getValue(), other.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
