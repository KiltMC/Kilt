package xyz.bluspring.kilt.forgeinjects.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.core.MappedRegistryInjection;

@Mixin(MappedRegistry.class)
public class MappedRegistryInject<T> {
    @Inject(method = "registerMapping(ILnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder;", at = @At("HEAD"))
    public void kilt$markRegistryAsKnown(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder<T>> cir) {
        MappedRegistryInjection.getKnownRegistries().add(((Registry) (Object) this).key().location());
    }
}
