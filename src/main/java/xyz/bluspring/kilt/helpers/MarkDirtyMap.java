package xyz.bluspring.kilt.helpers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

// Custom wrapping map implementation which runs `markDirtyFunction` whenever modified in any way.
public class MarkDirtyMap<K, V> implements Map<K, V> {

    private final Map<K, V> parent;
    private final Runnable markDirtyFunction;

    public MarkDirtyMap(Map<K, V> parent, Runnable markDirtyFunction) {
        this.parent = parent;
        this.markDirtyFunction = markDirtyFunction;
    }

    public void markDirty() {
        this.markDirtyFunction.run();
    }

    private <T> T markDirtyAfter(Supplier<T> runner, T newValue) {
        return markDirtyAfter(runner, newValue, true);
    }

    private <T> T markDirtyAfter(Supplier<T> runner, T newValue, boolean extraCondition) {
        var oldValue = runner.get();
        if (newValue != oldValue && extraCondition) {
            markDirty();
        }
        return oldValue;
    }

    private <T> T markDirtyAfter(Supplier<T> runner) {
        var oldValue = runner.get();
        markDirty();
        return oldValue;
    }

    private boolean markDirtyAfter(BooleanSupplier runner) {
        if (runner.getAsBoolean()) {
            markDirty();
            return true;
        } else {
            return false;
        }
    }

    private void markDirtyIf(Runnable runner, boolean shouldMarkDirty) {
        runner.run();
        if (shouldMarkDirty) {
            markDirty();
        }
    }

    private <T> T markDirtyIf(Supplier<T> runner, boolean shouldMarkDirty) {
        var result = runner.get();
        if (shouldMarkDirty) {
            markDirty();
        }
        return result;
    }

    @Override
    public int size() {
        return parent.size();
    }

    @Override
    public boolean isEmpty() {
        return parent.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return parent.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return parent.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return parent.get(key);
    }

    @Override
    public @Nullable V put(K key, V value) {
        return markDirtyAfter(() -> parent.put(key, value), value);
    }

    @Override
    public V remove(Object key) {
        return markDirtyAfter(() -> parent.remove(key), null);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        markDirtyIf(() -> parent.putAll(m), !m.isEmpty());
    }

    @Override
    public void clear() {
        markDirtyIf(parent::clear, !isEmpty());
    }

    @Override
    public @NotNull Set<K> keySet() {
        return new MarkDirtySet<>(parent.keySet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return new MarkDirtyCollection<>(parent.values());
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return new MarkDirtyEntrySet(parent.entrySet());
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return parent.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        parent.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        parent.replaceAll(function);
        markDirty();
    }

    @Override
    public @Nullable V putIfAbsent(K key, V value) {
        return markDirtyAfter(() -> putIfAbsent(key, value), value, !containsKey(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return markDirtyAfter(() -> parent.remove(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return markDirtyAfter(() -> parent.replace(key, oldValue, newValue));
    }

    @Override
    public @Nullable V replace(K key, V value) {
        return markDirtyAfter(() -> parent.replace(key, value), value, containsKey(key));
    }

    @Override
    public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        return markDirtyIf(() -> parent.computeIfAbsent(key, mappingFunction), !containsKey(key));
    }

    @Override
    public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return markDirtyIf(() -> parent.computeIfPresent(key, remappingFunction), containsKey(key));
    }

    @Override
    public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return markDirtyAfter(() -> parent.compute(key, remappingFunction));
    }

    @Override
    public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return markDirtyAfter(() -> parent.merge(key, value, remappingFunction));
    }

    public class MarkDirtyIterator<T> implements Iterator<T> {

        private final Iterator<T> parent;

        private MarkDirtyIterator(Iterator<T> parent) {
            this.parent = parent;
        }

        @Override
        public boolean hasNext() {
            return parent.hasNext();
        }

        @Override
        public T next() {
            return parent.next();
        }

        @Override
        public void remove() {
            parent.remove();
            markDirty();
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            parent.forEachRemaining(action);
        }
    }

    public class MarkDirtyCollection<T> implements Collection<T> {

        protected final Collection<T> parent;

        public MarkDirtyCollection(Collection<T> parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return parent.contains(o);
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return new MarkDirtyIterator<>(parent.iterator());
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            parent.forEach(action);
        }

        @Override
        public @NotNull Object @NotNull [] toArray() {
            return parent.toArray();
        }

        @Override
        public @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
            return parent.toArray(a);
        }

        @Override
        public <T1> T1[] toArray(@NotNull IntFunction<T1[]> generator) {
            return parent.toArray(generator);
        }

        @Override
        public boolean add(T t) {
            return markDirtyAfter(() -> parent.add(t));
        }

        @Override
        public boolean remove(Object o) {
            return markDirtyAfter(() -> parent.remove(o));
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return parent.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends T> c) {
            return markDirtyAfter(() -> parent.addAll(c));
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return markDirtyAfter(() -> parent.removeAll(c));
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super T> filter) {
            return markDirtyAfter(() -> parent.removeIf(filter));
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return markDirtyAfter(() -> parent.retainAll(c));
        }

        @Override
        public void clear() {
            markDirtyIf(parent::clear, !isEmpty());
        }

        @Override
        public @NotNull Spliterator<T> spliterator() {
            return parent.spliterator();
        }

        @Override
        public @NotNull Stream<T> stream() {
            return parent.stream();
        }

        @Override
        public @NotNull Stream<T> parallelStream() {
            return parent.parallelStream();
        }
    }

    public class MarkDirtySet<T> extends MarkDirtyCollection<T> implements Set<T> {
        public MarkDirtySet(Set<T> parent) {
            super(parent);
        }
    }

    public class MarkDirtyEntry implements Entry<K, V> {

        private final Entry<K, V> parent;

        public MarkDirtyEntry(Entry<K, V> parent) {
            this.parent = parent;
        }

        @Override
        public K getKey() {
            return parent.getKey();
        }

        @Override
        public V getValue() {
            return parent.getValue();
        }

        @Override
        public V setValue(V value) {
            return markDirtyAfter(() -> parent.setValue(value), value);
        }
    }

    public class MarkDirtyEntrySet extends MarkDirtySet<Entry<K, V>> {

        public MarkDirtyEntrySet(Set<Entry<K, V>> parent) {
            super(parent);
        }

        public class MarkDirtyEntrySetIterator implements Iterator<Entry<K, V>> {

            private final Iterator<Entry<K, V>> parent;

            public MarkDirtyEntrySetIterator(Iterator<Entry<K, V>> parent) {
                this.parent = parent;
            }

            @Override
            public boolean hasNext() {
                return parent.hasNext();
            }

            @Override
            public Entry<K, V> next() {
                var next = parent.next();
                if (next == null) return null;
                return new MarkDirtyEntry(next);
            }

            @Override
            public void remove() {
                parent.remove();
            }

            @Override
            public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                parent.forEachRemaining(e -> action.accept(new MarkDirtyEntry(e)));
            }
        }

        @Override
        public @NotNull Iterator<Entry<K, V>> iterator() {
            return new MarkDirtyEntrySetIterator(super.iterator());
        }

        @Override
        public void forEach(Consumer<? super Entry<K, V>> action) {
            super.forEach(e -> action.accept(new MarkDirtyEntry(e)));
        }

        private void fixArray(Object[] array) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] instanceof Map.Entry<?,?> entry && !(array[i] instanceof MarkDirtyMap<?,?>.MarkDirtyEntry)) {
                    //noinspection unchecked
                    array[i] = new MarkDirtyEntry((Map.Entry<K,V>) entry);
                }
            }
        }

        @Override
        public @NotNull Object @NotNull [] toArray() {
            var array = super.toArray();
            fixArray(array);
            return array;
        }

        @Override
        public @NotNull <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
            var array = super.toArray(a);
            fixArray(array);
            return array;
        }

        @Override
        public <T> T[] toArray(@NotNull IntFunction<T[]> generator) {
            var array = super.toArray(generator);
            fixArray(array);
            return array;
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super Entry<K, V>> filter) {
            return super.removeIf(e -> filter.test(new MarkDirtyEntry(e)));
        }

        public class MarkDirtyEntrySetSpliterator implements Spliterator<Entry<K, V>> {

            private final Spliterator<Entry<K, V>> parent;

            public MarkDirtyEntrySetSpliterator(Spliterator<Entry<K, V>> parent) {
                this.parent = parent;
            }

            @Override
            public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
                return parent.tryAdvance(e -> action.accept(new MarkDirtyEntry(e)));
            }

            @Override
            public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                parent.forEachRemaining(e -> action.accept(new MarkDirtyEntry(e)));
            }

            @Override
            public Spliterator<Entry<K, V>> trySplit() {
                return new MarkDirtyEntrySetSpliterator(parent.trySplit());
            }

            @Override
            public long estimateSize() {
                return parent.estimateSize();
            }

            @Override
            public long getExactSizeIfKnown() {
                return parent.getExactSizeIfKnown();
            }

            @Override
            public int characteristics() {
                return parent.characteristics();
            }

            @Override
            public boolean hasCharacteristics(int characteristics) {
                return parent.hasCharacteristics(characteristics);
            }

            @Override
            public Comparator<? super Entry<K, V>> getComparator() {
                return parent.getComparator();
            }
        }

        @Override
        public @NotNull Spliterator<Entry<K, V>> spliterator() {
            return new MarkDirtyEntrySetSpliterator(super.spliterator());
        }

        @Override
        public @NotNull Stream<Entry<K, V>> stream() {
            return super.stream().map(MarkDirtyEntry::new);
        }

        @Override
        public @NotNull Stream<Entry<K, V>> parallelStream() {
            return super.parallelStream().map(MarkDirtyEntry::new);
        }
    }

}
