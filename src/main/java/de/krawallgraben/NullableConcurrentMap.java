package de.krawallgraben;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("serial")
public class NullableConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {

    private enum NullPlaceholder {
        INSTANCE;
    }

    private static Object mask(Object value) {
        return value == null ? NullPlaceholder.INSTANCE : value;
    }

    @SuppressWarnings("unchecked")
    private V unmask(Object value) {
        return value == NullPlaceholder.INSTANCE ? null : (V) value;
    }

    public NullableConcurrentMap() {
        super();
    }

    public NullableConcurrentMap(int initialCapacity) {
        super(initialCapacity);
    }

    public NullableConcurrentMap(Map<? extends K, ? extends V> m) {
        super();
        this.putAll(m);
    }

    public NullableConcurrentMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public NullableConcurrentMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    // Basic Map Operations

    @Override
    public V get(Object key) {
        return unmask(super.get(key));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Object v = super.get(key);
        if (v == null) return defaultValue;
        return unmask(v);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(mask(value));
    }

    @Override
    public V put(K key, V value) {
        return unmask(super.put(key, (V) mask(value)));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return unmask(super.putIfAbsent(key, (V) mask(value)));
    }

    @Override
    public V replace(K key, V value) {
        return unmask(super.replace(key, (V) mask(value)));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace(key, (V) mask(oldValue), (V) mask(newValue));
    }

    @Override
    public V remove(Object key) {
        return unmask(super.remove(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(key, mask(value));
    }

    // Compute / Merge Operations

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        // If mappingFunction returns null, super.computeIfAbsent does nothing/returns null.
        // We replicate this behavior (no mask if null).
        return unmask(super.computeIfAbsent(key, k -> {
            V result = mappingFunction.apply(k);
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(super.computeIfPresent(key, (k, v) -> {
            V result = remappingFunction.apply(k, unmask(v));
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return unmask(super.compute(key, (k, v) -> {
            V result = remappingFunction.apply(k, unmask(v));
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        // value for merge cannot be null per Map spec.
        if (value == null) throw new NullPointerException();
        return unmask(super.merge(key, (V) mask(value), (v1, v2) -> {
            V result = remappingFunction.apply(unmask(v1), unmask(v2));
            return result == null ? null : (V) mask(result);
        }));
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        super.forEach((k, v) -> action.accept(k, unmask(v)));
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        super.replaceAll((k, v) -> {
            V result = function.apply(k, unmask(v));
            // ConcurrentHashMap.replaceAll throws NPE if function returns null.
            // But here, if we want to allow nulls, we should mask it.
            // However, if the original map behavior forbids nulls, replaceAll might expect non-nulls.
            // If function returns null, we store PLACEHOLDER.
            return (V) mask(result);
        });
    }

    // Views

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySetView();
    }

    @Override
    public Enumeration<V> elements() {
        Enumeration<V> superElements = super.elements();
        return new Enumeration<V>() {
            @Override
            public boolean hasMoreElements() {
                return superElements.hasMoreElements();
            }

            @Override
            public V nextElement() {
                return unmask(superElements.nextElement());
            }
        };
    }

    // ConcurrentHashMap specific methods (parallel operations)

    @Override
    public void forEachValue(long parallelismThreshold, Consumer<? super V> action) {
        super.forEachValue(parallelismThreshold, v -> action.accept(unmask(v)));
    }

    @Override
    public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
        super.forEachValue(parallelismThreshold, v -> transformer.apply(unmask(v)), action);
    }

    @Override
    public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
        return super.searchValues(parallelismThreshold, v -> searchFunction.apply(unmask(v)));
    }

    // Helper classes for views

    private class ValuesView extends AbstractCollection<V> {
        private final Collection<V> base = NullableConcurrentMap.super.values();

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

        // Spliterator could be overridden for better performance/correctness but Iterator is sufficient for correctness.
    }

    private class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
        private final Set<Map.Entry<K, V>> base = NullableConcurrentMap.super.entrySet();

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
            Object stored = NullableConcurrentMap.super.get(key);
            return stored != null && Objects.equals(unmask(stored), val);
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return NullableConcurrentMap.super.remove(e.getKey(), mask(e.getValue()));
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
            return entry.getKey();
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
