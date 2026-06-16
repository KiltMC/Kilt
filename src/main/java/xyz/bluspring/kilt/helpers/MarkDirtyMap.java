package xyz.bluspring.kilt.helpers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

// Custom wrapping map implementation which runs `markDirtyFunction` whenever modified in any way.
public class MarkDirtyMap<K, V> implements Map<K, V> {

    private final Map<K, V> parent;
    private final MarkDirtyContainer mdf;

    public MarkDirtyMap(Map<K, V> parent, Runnable mdf) {
        this.parent = parent;
        this.mdf = new MarkDirtyContainer(mdf);
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
        return mdf.markDirtyAfter(() -> parent.put(key, value), value);
    }

    @Override
    public V remove(Object key) {
        return mdf.markDirtyAfter(() -> parent.remove(key), null);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        mdf.markDirtyIf(() -> parent.putAll(m), !m.isEmpty());
    }

    @Override
    public void clear() {
        mdf.markDirtyIf(parent::clear, !isEmpty());
    }

    @Override
    public @NotNull Set<K> keySet() {
        return new MarkDirtySet<>(parent.keySet(), mdf);
    }

    @Override
    public @NotNull Collection<V> values() {
        return new MarkDirtyCollection<>(parent.values(), mdf);
    }

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        return new MarkDirtyEntrySet<>(parent.entrySet(), mdf);
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
        mdf.markDirty();
    }

    @Override
    public @Nullable V putIfAbsent(K key, V value) {
        return mdf.markDirtyAfter(() -> putIfAbsent(key, value), value, !containsKey(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return mdf.markDirtyAfter(() -> parent.remove(key, value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return mdf.markDirtyAfter(() -> parent.replace(key, oldValue, newValue));
    }

    @Override
    public @Nullable V replace(K key, V value) {
        return mdf.markDirtyAfter(() -> parent.replace(key, value), value, containsKey(key));
    }

    @Override
    public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        return mdf.markDirtyIf(() -> parent.computeIfAbsent(key, mappingFunction), !containsKey(key));
    }

    @Override
    public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mdf.markDirtyIf(() -> parent.computeIfPresent(key, remappingFunction), containsKey(key));
    }

    @Override
    public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mdf.markDirtyAfter(() -> parent.compute(key, remappingFunction));
    }

    @Override
    public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return mdf.markDirtyAfter(() -> parent.merge(key, value, remappingFunction));
    }

    public record MarkDirtyContainer(Runnable markDirtyFunction) {

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

    }

    public static class MarkDirtyIterator<T> implements Iterator<T> {

        private final Iterator<T> parent;
        private final MarkDirtyContainer mdf;

        private MarkDirtyIterator(Iterator<T> parent, MarkDirtyContainer mdf) {
            this.parent = parent;
            this.mdf = mdf;
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
            mdf.markDirty();
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            parent.forEachRemaining(action);
        }
    }

    public static class MarkDirtyCollection<T> implements Collection<T> {

        protected final Collection<T> parent;
        protected final MarkDirtyContainer mdf;

        public MarkDirtyCollection(Collection<T> parent, MarkDirtyContainer mdf) {
            this.parent = parent;
            this.mdf = mdf;
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
            return new MarkDirtyIterator<>(parent.iterator(), mdf);
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
            return mdf.markDirtyAfter(() -> parent.add(t));
        }

        @Override
        public boolean remove(Object o) {
            return mdf.markDirtyAfter(() -> parent.remove(o));
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return parent.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends T> c) {
            return mdf.markDirtyAfter(() -> parent.addAll(c));
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return mdf.markDirtyAfter(() -> parent.removeAll(c));
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super T> filter) {
            return mdf.markDirtyAfter(() -> parent.removeIf(filter));
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return mdf.markDirtyAfter(() -> parent.retainAll(c));
        }

        @Override
        public void clear() {
            mdf.markDirtyIf(parent::clear, !isEmpty());
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

    public static class MarkDirtySet<T> extends MarkDirtyCollection<T> implements Set<T> {
        public MarkDirtySet(Set<T> parent, MarkDirtyContainer mdf) {
            super(parent, mdf);
        }
    }

    public static class MarkDirtyEntry<K ,V> implements Entry<K, V> {

        private final Entry<K, V> parent;
        private final MarkDirtyContainer mdf;

        public MarkDirtyEntry(Entry<K, V> parent, MarkDirtyContainer mdf) {
            this.parent = parent;
            this.mdf = mdf;
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
            return mdf.markDirtyAfter(() -> parent.setValue(value), value);
        }
    }

    public static class MarkDirtyEntrySet<K, V> extends MarkDirtySet<Entry<K, V>> {

        public MarkDirtyEntrySet(Set<Entry<K, V>> parent, MarkDirtyContainer mdf) {
            super(parent, mdf);
        }

        public static class MarkDirtyEntrySetIterator<K, V> implements Iterator<Entry<K, V>> {

            private final Iterator<Entry<K, V>> parent;
            private final MarkDirtyContainer mdf;

            public MarkDirtyEntrySetIterator(Iterator<Entry<K, V>> parent, MarkDirtyContainer mdf) {
                this.parent = parent;
                this.mdf = mdf;
            }

            @Override
            public boolean hasNext() {
                return parent.hasNext();
            }

            @Override
            public Entry<K, V> next() {
                var next = parent.next();
                if (next == null) return null;
                return new MarkDirtyEntry<>(next, mdf);
            }

            @Override
            public void remove() {
                parent.remove();
            }

            @Override
            public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                parent.forEachRemaining(e -> action.accept(new MarkDirtyEntry<>(e, mdf)));
            }
        }

        @Override
        public @NotNull Iterator<Entry<K, V>> iterator() {
            return new MarkDirtyEntrySetIterator<>(super.iterator(), mdf);
        }

        @Override
        public void forEach(Consumer<? super Entry<K, V>> action) {
            super.forEach(e -> action.accept(new MarkDirtyEntry<>(e, mdf)));
        }

        private void fixArray(Object[] array) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] instanceof Map.Entry<?,?> entry && !(array[i] instanceof MarkDirtyMap.MarkDirtyEntry<?,?>)) {
                    //noinspection unchecked
                    array[i] = new MarkDirtyEntry<>((Map.Entry<K,V>) entry, mdf);
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
            return super.removeIf(e -> filter.test(new MarkDirtyEntry<>(e, mdf)));
        }

        private class MarkDirtyEntrySetSpliterator implements Spliterator<Entry<K, V>> {

            private final Spliterator<Entry<K, V>> parent;

            public MarkDirtyEntrySetSpliterator(Spliterator<Entry<K, V>> parent) {
                this.parent = parent;
            }

            @Override
            public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
                return parent.tryAdvance(e -> action.accept(new MarkDirtyEntry<>(e, mdf)));
            }

            @Override
            public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                parent.forEachRemaining(e -> action.accept(new MarkDirtyEntry<>(e, mdf)));
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
            return super.stream().map(e -> new MarkDirtyEntry<>(e, mdf));
        }

        @Override
        public @NotNull Stream<Entry<K, V>> parallelStream() {
            return super.parallelStream().map(e -> new MarkDirtyEntry<>(e, mdf));
        }
    }

}
