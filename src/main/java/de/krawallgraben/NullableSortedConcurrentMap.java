package de.krawallgraben;

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
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("serial")
public class NullableSortedConcurrentMap<K, V> extends ConcurrentSkipListMap<K, V> {

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

    // Internal comparator wrapper that handles NullPlaceholder
    private static class NullSafeComparator<K> implements Comparator<Object> {
        private final Comparator<? super K> delegate;

        NullSafeComparator(Comparator<? super K> delegate) {
            this.delegate = delegate;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            if (o1 == NullPlaceholder.INSTANCE) {
                return o2 == NullPlaceholder.INSTANCE ? 0 : -1; // null is smaller
            }
            if (o2 == NullPlaceholder.INSTANCE) {
                return 1; // o1 is greater
            }

            if (delegate != null) {
                return delegate.compare((K) o1, (K) o2);
            } else {
                return ((Comparable<? super K>) o1).compareTo((K) o2);
            }
        }
    }

    public NullableSortedConcurrentMap() {
        super(new NullSafeComparator<K>(null));
    }

    public NullableSortedConcurrentMap(Comparator<? super K> comparator) {
        super(new NullSafeComparator<K>(comparator));
    }

    public NullableSortedConcurrentMap(Map<? extends K, ? extends V> m) {
        this();
        this.putAll(m);
    }

    public NullableSortedConcurrentMap(SortedMap<K, ? extends V> m) {
        this(m.comparator());
        this.putAll(m);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Comparator<? super K> comparator() {
        Comparator<? super K> internal = super.comparator();
        if (internal instanceof NullSafeComparator) {
            return ((NullSafeComparator<K>) internal).delegate;
        }
        return internal;
    }

    @Override
    public V get(Object key) {
        return unmask(super.get(mask(key)));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Object v = super.get(mask(key));
        if (v == null) return defaultValue;
        return unmask(v);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(mask(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(mask(value));
    }

    @Override
    public V put(K key, V value) {
        return unmask(super.put((K) mask(key), (V) mask(value)));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return unmask(super.putIfAbsent((K) mask(key), (V) mask(value)));
    }

    @Override
    public V replace(K key, V value) {
        return unmask(super.replace((K) mask(key), (V) mask(value)));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace((K) mask(key), (V) mask(oldValue), (V) mask(newValue));
    }

    @Override
    public V remove(Object key) {
        return unmask(super.remove(mask(key)));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(mask(key), mask(value));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return unmask(super.computeIfAbsent((K) mask(key), k -> {
            V result = mappingFunction.apply(unmask(k));
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(super.computeIfPresent((K) mask(key), (k, v) -> {
            V result = remappingFunction.apply(unmask(k), unmask(v));
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(super.compute((K) mask(key), (k, v) -> {
            V result = remappingFunction.apply(unmask(k), unmask(v));
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null) throw new NullPointerException();
        return unmask(super.merge((K) mask(key), (V) mask(value), (v1, v2) -> {
            V result = remappingFunction.apply(unmask(v1), unmask(v2));
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public K firstKey() {
        return unmask(super.firstKey());
    }

    @Override
    public K lastKey() {
        return unmask(super.lastKey());
    }

    @Override
    public Map.Entry<K, V> pollFirstEntry() {
        return unmaskEntry(super.pollFirstEntry());
    }

    @Override
    public Map.Entry<K, V> pollLastEntry() {
        return unmaskEntry(super.pollLastEntry());
    }

    @Override
    public Map.Entry<K, V> firstEntry() {
        return unmaskEntry(super.firstEntry());
    }

    @Override
    public Map.Entry<K, V> lastEntry() {
        return unmaskEntry(super.lastEntry());
    }

    @Override
    public Map.Entry<K, V> lowerEntry(K key) {
        return unmaskEntry(super.lowerEntry((K) mask(key)));
    }

    @Override
    public K lowerKey(K key) {
        return unmask(super.lowerKey((K) mask(key)));
    }

    @Override
    public Map.Entry<K, V> floorEntry(K key) {
        return unmaskEntry(super.floorEntry((K) mask(key)));
    }

    @Override
    public K floorKey(K key) {
        return unmask(super.floorKey((K) mask(key)));
    }

    @Override
    public Map.Entry<K, V> ceilingEntry(K key) {
        return unmaskEntry(super.ceilingEntry((K) mask(key)));
    }

    @Override
    public K ceilingKey(K key) {
        return unmask(super.ceilingKey((K) mask(key)));
    }

    @Override
    public Map.Entry<K, V> higherEntry(K key) {
        return unmaskEntry(super.higherEntry((K) mask(key)));
    }

    @Override
    public K higherKey(K key) {
        return unmask(super.higherKey((K) mask(key)));
    }

    // Views

    @Override
    public NavigableSet<K> keySet() {
        return new KeySetView(super.keySet());
    }

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySetView();
    }

    private Map.Entry<K, V> unmaskEntry(Map.Entry<K, V> entry) {
        if (entry == null) return null;
        return new EntryWrapper(entry);
    }

    // Helpers and Views

    private class KeySetView extends AbstractSet<K> implements NavigableSet<K> {
        private final NavigableSet<K> base;

        KeySetView(NavigableSet<K> base) {
            this.base = base;
        }

        @Override
        public Iterator<K> iterator() {
            Iterator<K> it = base.iterator();
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

        // Implementing NavigableSet interface methods
        @Override public K lower(K k) { return unmask(base.lower((K)mask(k))); }
        @Override public K floor(K k) { return unmask(base.floor((K)mask(k))); }
        @Override public K ceiling(K k) { return unmask(base.ceiling((K)mask(k))); }
        @Override public K higher(K k) { return unmask(base.higher((K)mask(k))); }
        @Override public K pollFirst() { return unmask(base.pollFirst()); }
        @Override public K pollLast() { return unmask(base.pollLast()); }
        @Override public Comparator<? super K> comparator() { return NullableSortedConcurrentMap.this.comparator(); }
        @Override public K first() { return unmask(base.first()); }
        @Override public K last() { return unmask(base.last()); }

        @Override
        public NavigableSet<K> descendingSet() {
             return new KeySetView(base.descendingSet());
        }

        @Override
        public Iterator<K> descendingIterator() {
            Iterator<K> it = base.descendingIterator();
             return new Iterator<K>() {
                @Override public boolean hasNext() { return it.hasNext(); }
                @Override public K next() { return unmask(it.next()); }
                @Override public void remove() { it.remove(); }
            };
        }

        @Override
        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
             return new KeySetView(base.subSet((K)mask(fromElement), fromInclusive, (K)mask(toElement), toInclusive));
        }

        @Override
        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
             return new KeySetView(base.headSet((K)mask(toElement), inclusive));
        }

        @Override
        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
             return new KeySetView(base.tailSet((K)mask(fromElement), inclusive));
        }

        @Override
        public SortedSet<K> subSet(K fromElement, K toElement) {
             return subSet(fromElement, true, toElement, false);
        }

        @Override
        public SortedSet<K> headSet(K toElement) {
             return headSet(toElement, false);
        }

        @Override
        public SortedSet<K> tailSet(K fromElement) {
             return tailSet(fromElement, true);
        }
    }

    private class ValuesView extends AbstractCollection<V> {
        private final Collection<V> base = NullableSortedConcurrentMap.super.values();

        @Override
        public Iterator<V> iterator() {
            Iterator<V> it = base.iterator();
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

    private class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
        private final Set<Map.Entry<K, V>> base = NullableSortedConcurrentMap.super.entrySet();

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            Iterator<Map.Entry<K, V>> it = base.iterator();
            return new Iterator<Map.Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Map.Entry<K, V> next() {
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
            Object key = e.getKey();
            Object val = e.getValue();
            Object stored = NullableSortedConcurrentMap.super.get(mask(key));
            return stored != null && Objects.equals(unmask(stored), val);
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return NullableSortedConcurrentMap.super.remove(mask(e.getKey()), mask(e.getValue()));
        }

        @Override
        public void clear() {
            base.clear();
        }
    }

    private class EntryWrapper implements Map.Entry<K, V> {
        private final Map.Entry<K, V> entry;

        EntryWrapper(Map.Entry<K, V> entry) {
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
            return unmask(entry.setValue((V) mask(value)));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
            return Objects.equals(getKey(), other.getKey()) &&
                   Objects.equals(getValue(), other.getValue());
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
