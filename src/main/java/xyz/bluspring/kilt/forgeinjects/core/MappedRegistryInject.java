package xyz.bluspring.kilt.forgeinjects.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.core.MappedRegistryInjection;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryInject<T> extends WritableRegistry<T> implements MappedRegistryInjection {
    @Shadow public boolean frozen;

    @Shadow @Final @Nullable private Function<T, Holder.Reference<T>> customHolderProvider;

    @Shadow @Nullable private Map<T, Holder.Reference<T>> intrusiveHolderCache;

    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @CreateStatic
    private static final Set<ResourceLocation> KNOWN = MappedRegistryInjection.knownRegistries;

    public MappedRegistryInject(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    @CreateStatic
    private static Set<ResourceLocation> getKnownRegistries() {
        return MappedRegistryInjection.getKnownRegistries();
    }

    @Inject(method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder;", at = @At("HEAD"))
    public void kilt$markRegistryAsKnown(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<T>> cir) {
        markKnown();
    }

    @WrapOperation(method = "containsKey(Lnet/minecraft/resources/ResourceKey;)Z", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z"))
    private boolean kilt$checkIsBound(Map instance, Object o, Operation<Boolean> original, @Local(argsOnly = true) ResourceKey<T> resourceKey) {
        return original.call(instance, o) && this.byKey.get(resourceKey).isBound();
    }

    @Override
    public void markKnown() {
        KNOWN.add(((Registry) (Object) this).key().location());
    }

    @Override
    public void unfreeze() {
        this.frozen = false;
        if (this.customHolderProvider != null && this.intrusiveHolderCache == null)
            this.intrusiveHolderCache = new IdentityHashMap<>();
    }

    @ModifyVariable(method = {
        "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;",
        "containsKey(Lnet/minecraft/resources/ResourceKey;)Z",
        "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;",
        "getOrCreateHolderOrThrow"
    }, at = @At("HEAD"), argsOnly = true)
    private ResourceKey<T> kilt$tryGetAlias(ResourceKey<T> value) {
        var registry = RegistryManager.ACTIVE.getRegistry(this.key());

        if (registry != null) {
            var alias = registry.kilt$getAlias(value.location());

            if (alias != null)
                return ResourceKey.create(this.key(), alias);
        }

        return value;
    }

    @ModifyVariable(method = {
        "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
        "containsKey(Lnet/minecraft/resources/ResourceLocation;)Z"
    }, at = @At("HEAD"), argsOnly = true)
    private ResourceLocation kilt$tryGetAlias(ResourceLocation value) {
        var registry = RegistryManager.ACTIVE.getRegistry(this.key());

        if (registry != null) {
            var alias = registry.kilt$getAlias(value);

            if (alias != null)
                return alias;
        }

        return value;
    }
}
