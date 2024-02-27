package xyz.bluspring.kilt.forgeinjects.core;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistrySynchronization.class)
public class RegistrySynchronizationInject {
    @WrapOperation(method = "method_45958", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;"))
    private static ImmutableMap<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> kilt$redirectNetworkRegistries(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> instance, Operation<ImmutableMap<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>>> original) {
        return (ImmutableMap<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>>) DataPackRegistriesHooks.grabNetworkableRegistries(instance);
    }
}
