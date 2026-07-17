package xyz.bluspring.kilt.injects.util.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.injections.util.random.WeightedListInjection;

import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

@Mixin(WeightedList.class)
public abstract class WeightedListInject {
    @Mixin(WeightedList.Builder.class)
    public abstract static class BuilderInject<E> implements WeightedListInjection.BuilderInjection<E> {
        @Shadow @Final private ImmutableList.Builder<Weighted<E>> result;
        @Unique private final List<Weighted<E>> kilt$removed = new ArrayList<>();

        @Override
        public WeightedList.Builder<E> addAll(WeightedList<E> values) {
            this.addAll(values.unwrap());
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> addAll(Collection<Weighted<E>> values) {
            this.result.addAll(values);
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> remove(E value) {
            this.removeIf(weighted -> weighted.value().equals(value));
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> remove(Weighted<E> value) {
            this.kilt$removed.add(value);
            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public WeightedList.Builder<E> removeIf(Predicate<Weighted<E>> filter) {
            for (Weighted<E> weighted : this.result.build()) {
                if (filter.test(weighted)) {
                    this.kilt$removed.add(weighted);
                }
            }

            return (WeightedList.Builder<E>) (Object) this;
        }

        @Override
        public List<Weighted<E>> getList() {
            return this.kilt$withoutRemoved(this.result.build());
        }

        @Unique
        private List<Weighted<E>> kilt$withoutRemoved(List<? extends Weighted<E>> original) {
            var list = new ArrayList<>(original);
            for (Weighted<E> weighted : this.kilt$removed) {
                list.remove(weighted);
            }

            return Collections.unmodifiableList(list);
        }

        @ModifyArg(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/WeightedList;<init>(Ljava/util/List;)V"))
        private List<? extends Weighted<E>> kilt$buildWithoutRemoved(List<? extends Weighted<E>> items) {
            return this.kilt$withoutRemoved(items);
        }
    }
}
