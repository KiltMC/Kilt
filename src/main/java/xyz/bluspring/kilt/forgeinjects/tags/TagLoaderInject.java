package xyz.bluspring.kilt.forgeinjects.tags;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.tags.TagFileInjection;
import xyz.bluspring.kilt.injections.tags.TagLoaderInjection;

import java.util.*;
import java.util.function.Consumer;

@Mixin(TagLoader.class)
public abstract class TagLoaderInject<T> {
    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void kilt$addRemoveEntries(ResourceManager resourceManager, CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir, @Local TagFile tagFile, @Local List<TagLoader.EntryWithSource> list, @Local String s) {
        ((TagFileInjection) (Object) tagFile).remove().forEach(e -> list.add(TagLoaderInjection.EntryWithSourceInjection.create(e, s, true)));
    }

    @Inject(method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;", at = @At("HEAD"))
    private <E> void kilt$storeRemovedSet(TagEntry.Lookup<E> lookup, List<TagLoader.EntryWithSource> entries, CallbackInfoReturnable<Either<Collection<TagLoader.EntryWithSource>, Collection<E>>> cir, @Share("removed") LocalRef<Set<E>> removed) {
        removed.set(new LinkedHashSet<>());
    }

    @ModifyArg(method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagEntry;build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/function/Consumer;)Z"))
    private <E> Consumer<E> kilt$removeFromBuilderIfNeeded(Consumer<E> consumer, @Local ImmutableSet.Builder<E> builder, @Share("removed") LocalRef<Set<E>> removed, @Local TagLoader.EntryWithSource entryWithSource) {
        if (((TagLoaderInjection.EntryWithSourceInjection) (Object) entryWithSource).remove())
            return removed.get()::add;

        return consumer;
    }

    @WrapWithCondition(method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> boolean kilt$avoidAddingRemovedEntries(List<E> instance, E e, @Local TagLoader.EntryWithSource entryWithSource) {
        return !((TagLoaderInjection.EntryWithSourceInjection) (Object) entryWithSource).remove();
    }

    @ModifyArg(method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;right(Ljava/lang/Object;)Lcom/mojang/datafixers/util/Either;", remap = false))
    private <R> R kilt$removeEntriesFromBuilder(R value, @Share("removed") LocalRef<Set<T>> removed) {
        var set = (ImmutableSet<T>) value;
        var list = new ArrayList<>(List.copyOf(set));

        list.removeAll(removed.get());

        return (R) list;
    }

    @Mixin(TagLoader.EntryWithSource.class)
    public abstract static class EntryWithSourceInject implements TagLoaderInjection.EntryWithSourceInjection {
        @Unique private boolean remove = false;

        public EntryWithSourceInject(TagEntry entry, String source) {}

        @CreateInitializer
        public EntryWithSourceInject(TagEntry entry, String source, boolean remove) {
            this(entry, source);
            this.remove = remove;
        }

        @Override
        public boolean remove() {
            return remove;
        }

        @Override
        public void kilt$setRemove(boolean remove) {
            this.remove = remove;
        }
    }
}
