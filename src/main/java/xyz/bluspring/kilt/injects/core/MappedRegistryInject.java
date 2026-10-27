package xyz.bluspring.kilt.injects.core;

import java.util.Map;
import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.core.MappedRegistryInjection;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

@Mixin(MappedRegistry.class)
@Extends(BaseMappedRegistry.class)
public abstract class MappedRegistryInject<T> implements WritableRegistry<T>, MappedRegistryInjection<T> {
    @Shadow private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @Shadow protected abstract void validateWrite();
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow @Final private Map<Identifier, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow private MappedRegistry.TagSet<T> allTags;
    @Shadow protected abstract void validateWrite(ResourceKey<T> key);

    @Shadow
    private boolean frozen;
    @Unique private final ThreadLocal<Integer> kilt$id = new ThreadLocal<>();

    @Override
    public Holder.Reference<T> register(int id, ResourceKey<T> key, T value, RegistrationInfo info) {
        if (id > this.getMaxId()) {
            throw new IllegalStateException(String.format(java.util.Locale.ENGLISH, "Invalid id %d - maximum id range of %d exceeded.", id, this.getMaxId()));
        }

        this.kilt$id.set(id);
        var holder = register(key, value, info);
        this.kilt$id.remove();

        return holder;
    }

    @ModifyExpressionValue(method = "register", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", ordinal = 0))
    private <V> V kilt$bindReferenceValueImmediately(V original, @Local(argsOnly = true) T value) {
        if (original instanceof Holder.Reference<?> reference) {
            ((Holder.Reference<T>) reference).bindValue(value);
        }

        return original;
    }

    @ModifyExpressionValue(method = "register", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectList;size()I", ordinal = 0))
    private int kilt$useForgeIdValue(int original) {
        if (this.kilt$id.get() != null) {
            return this.kilt$id.get();
        }

        return original;
    }

    @Inject(method = "register", at = @At("TAIL"))
    private void kilt$callNeoAddCallbacks(ResourceKey<T> key, T value, RegistrationInfo registrationInfo, CallbackInfoReturnable<Holder.Reference<T>> cir, @Local(name = "newId") int id) {
        ((BaseMappedRegistry<T>) (Object) this).addCallbacks.forEach(callback -> callback.onAdd(this, id, key, value));
    }

    // Kilt: Mods like WorldWeaver have broken code and are passing null here, but we need to support them anyway, unfortunately.
    @Inject(method = "getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("HEAD"), order = 900, cancellable = true)
    private void kilt$resolveAsNullWhenEmpty(ResourceKey<T> key, CallbackInfoReturnable<T> cir) {
        if (key == null)
            cir.setReturnValue(null);
    }

    @Inject(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("HEAD"), order = 900, cancellable = true)
    private void kilt$resolveAsEmptyWhenEmpty(ResourceKey<T> key, CallbackInfoReturnable<Optional<Holder.Reference<T>>> cir) {
        if (key == null)
            cir.setReturnValue(Optional.empty());
    }

    @ModifyVariable(method = {"getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", "getOrCreateHolderOrThrow"}, at = @At("HEAD"), argsOnly = true)
    private ResourceKey<T> kilt$resolveKey(ResourceKey<T> value) {
        return resolve(value);
    }

    @ModifyVariable(method = {"getValue(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;", "get(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;"}, at = @At("HEAD"), argsOnly = true)
    private Identifier kilt$resolveLocation(Identifier value) {
        if (value != null)
            return resolve(value);
        else
            return null;
    }

    @Override
    public void unfreeze(boolean clearTags) {
        if (clearTags)
            this.allTags = MappedRegistry.TagSet.unbound();

        this.frozen = false;
    }

    // Kilt: force store unregisteredIntrusiveHolders
    @Inject(method = "freeze", at = @At("HEAD"))
    private void kilt$disableNullRegister(CallbackInfoReturnable<Registry<T>> cir, @Share("kilt$unregisteredIntrusiveHolders") LocalRef<Map<T, Holder.Reference<T>>> unregistered) {
        unregistered.set(this.unregisteredIntrusiveHolders);
    }

    @Inject(method = "freeze", at = @At(value = "RETURN", ordinal = 1))
    private void kilt$forceSetUnregistered(CallbackInfoReturnable<Registry<T>> cir, @Share("kilt$unregisteredIntrusiveHolders") LocalRef<Map<T, Holder.Reference<T>>> unregistered) {
        this.unregisteredIntrusiveHolders = unregistered.get();
        ((BaseMappedRegistry<T>) (Object) this).bakeCallbacks.forEach(callback -> callback.onBake(this));
    }

    @Override
    public void clear(boolean full) {
        this.validateWrite();
        ((BaseMappedRegistry<T>) (Object) this).clearCallbacks.forEach(callback -> callback.onClear(this, full));
        this.kilt$clear(full);
        this.byId.clear();
        this.toId.clear();
        if (full) {
            this.byLocation.clear();
            this.byKey.clear();
            this.byValue.clear();
            this.allTags = MappedRegistry.TagSet.unbound();

            if (this.unregisteredIntrusiveHolders != null) {
                this.unregisteredIntrusiveHolders.clear();
                this.unregisteredIntrusiveHolders = null;
            }
        }
    }

    @Override
    public void registerIdMapping(ResourceKey<T> key, int id) {
        this.validateWrite(key);

        if (id > this.getMaxId())
            throw new IllegalStateException(String.format(java.util.Locale.ENGLISH, "Invalid id %d - maximum id range of %d exceeded.", id, this.getMaxId()));

        if (id <= 0 && id < this.byId.size() && this.byId.get(id) != null)
            throw new IllegalStateException("Duplicate id " + id + " for " + key + " and " + this.getKey(this.byId.get(id).value()));

        var holder = this.byKey.get(key);
        while (this.byId.size() < (id + 1))
            this.byId.add(null);

        this.byId.set(id, holder);
        this.toId.put(holder.value(), id);
    }

    @Override
    public int getId(Identifier name) {
        return getId(getValue(name));
    }

    @Override
    public int getId(ResourceKey<T> key) {
        return getId(getValue(key));
    }

    @Override
    public boolean containsValue(T value) {
        return this.byValue.containsKey(value);
    }
}
