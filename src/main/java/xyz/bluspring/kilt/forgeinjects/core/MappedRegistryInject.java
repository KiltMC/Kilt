package xyz.bluspring.kilt.forgeinjects.core;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.core.MappedRegistryInjection;

import java.util.Map;
import java.util.Set;

@Mixin(MappedRegistry.class)
public class MappedRegistryInject<T> implements MappedRegistryInjection {
    @Shadow public boolean frozen;

    @Shadow @Nullable public Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @CreateStatic
    private static final Set<ResourceLocation> knownRegistries = MappedRegistryInjection.knownRegistries;

    @CreateStatic
    private static Set<ResourceLocation> getKnownRegistries() {
        return MappedRegistryInjection.getKnownRegistries();
    }

    @Inject(method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder;", at = @At("HEAD"))
    public void kilt$markRegistryAsKnown(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<T>> cir) {
        markKnown();
    }

    @Override
    public void markKnown() {
        MappedRegistryInjection.getKnownRegistries().add(((Registry) (Object) this).key().location());
    }

    @Override
    public void unfreeze() {
        this.frozen = false;
    }

    // Kilt: force store unregisteredIntrusiveHolders
    @Inject(method = "freeze", at = @At("HEAD"))
    private void kilt$disableNullRegister(CallbackInfoReturnable<Registry<T>> cir, @Share("kilt$unregisteredIntrusiveHolders") LocalRef<Map<T, Holder.Reference<T>>> unregistered) {
        unregistered.set(this.unregisteredIntrusiveHolders);
    }

    @Inject(method = "freeze", at = @At("RETURN"))
    private void kilt$forceSetUnregistered(CallbackInfoReturnable<Registry<T>> cir, @Share("kilt$unregisteredIntrusiveHolders") LocalRef<Map<T, Holder.Reference<T>>> unregistered) {
        this.unregisteredIntrusiveHolders = unregistered.get();
    }
}
