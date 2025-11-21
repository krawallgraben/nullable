package de.krawallgraben;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("serial")
public class NullableConcurrentMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    private final ConcurrentHashMap<Object, Object> internalMap;

    private enum NullPlaceholder {
        INSTANCE;
        @Override public String toString() { return "null"; }
    }

    private static Object mask(Object value) {
        return value == null ? NullPlaceholder.INSTANCE : value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T unmask(Object value) {
        return value == NullPlaceholder.INSTANCE ? null : (T) value;
    }

    public NullableConcurrentMap() {
        this.internalMap = new ConcurrentHashMap<>();
    }

    public NullableConcurrentMap(int initialCapacity) {
        this.internalMap = new ConcurrentHashMap<>(initialCapacity);
    }

    public NullableConcurrentMap(Map<? extends K, ? extends V> m) {
        this.internalMap = new ConcurrentHashMap<>();
        this.putAll(m);
    }

    public NullableConcurrentMap(int initialCapacity, float loadFactor) {
        this.internalMap = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    public NullableConcurrentMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this.internalMap = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    // Map / ConcurrentMap Implementation

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
        // We mask the key to check presence
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

    @Override
    public Set<K> keySet() {
        return new KeySetView();
    }

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySetView();
    }

    // ConcurrentMap specific

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

    // Default methods from Map that need overriding for atomicity/correctness with masking

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
        return unmask(internalMap.computeIfAbsent(mask(key), k -> {
            V result = mappingFunction.apply(unmask(k));
            return result == null ? null : mask(result);
            // Note: computeIfAbsent in Map says if func returns null, no mapping is recorded.
            // In internalMap, if we return null, it does nothing.
            // BUT we want to support null values.
            // If func returns null, we want to store PLACEHOLDER?
            // Map contract: "If the function returns null no mapping is recorded."
            // So we return null here too.
        }));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(internalMap.computeIfPresent(mask(key), (k, v) -> {
            V result = remappingFunction.apply(unmask(k), unmask(v));
            return result == null ? null : mask(result);
        }));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(internalMap.compute(mask(key), (k, v) -> {
            V result = remappingFunction.apply(unmask(k), unmask(v));
            return result == null ? null : mask(result);
        }));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null) throw new NullPointerException();
        return unmask(internalMap.merge(mask(key), mask(value), (v1, v2) -> {
            V result = remappingFunction.apply(unmask(v1), unmask(v2));
            return result == null ? null : mask(result);
        }));
    }

    // Views

    private class KeySetView extends AbstractSet<K> {
        private final Set<Object> base = internalMap.keySet();

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
        public void clear() {
            base.clear();
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
            Object key = mask(e.getKey());
            Object val = internalMap.get(key);
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

    private class EntryWrapper implements Map.Entry<K, V> {
        private final Map.Entry<Object, Object> entry;

        EntryWrapper(Map.Entry<Object, Object> entry) {
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
