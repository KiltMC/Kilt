package xyz.bluspring.kilt.injections.util.random;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

public interface WeightedListInjection<E> {

    interface BuilderInjection<E> {
        default WeightedList.Builder<E> add(Weighted<E> value) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "add");
        }

        default WeightedList.Builder<E> addAll(WeightedList<E> values) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "addAll");
        }

        default WeightedList.Builder<E> addAll(Collection<Weighted<E>> values) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "addAll");
        }

        default WeightedList.Builder<E> remove(Weighted<E> value) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "remove");
        }

        default WeightedList.Builder<E> remove(E value) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "remove");
        }

        default WeightedList.Builder<E> removeIf(Predicate<Weighted<E>> filter) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "removeIf");
        }

        default List<Weighted<E>> getList() {
            throw KiltHelper.createMixinException(BuilderInjection.class, "getList");
        }
    }
}
