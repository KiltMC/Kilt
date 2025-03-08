package xyz.bluspring.kilt.forgeinjects.resources;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(RegistryLoader.class)
public abstract class RegistryLoaderInject {
    @Redirect(method = "overrideRegistryFromResources", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private <E> Set<Map.Entry<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>>> kilt$filterRegistryThunks(Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> instance) {
        return Set.copyOf(ForgeHooks.filterThunks(instance));
    }

    @Redirect(method = "overrideElementFromResources(Lnet/minecraft/core/WritableRegistry;Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;Lnet/minecraft/resources/ResourceKey;Ljava/util/Optional;Lcom/mojang/serialization/DynamicOps;)Lcom/mojang/serialization/DataResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;getOrCreateHolderOrThrow(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/Holder;"))
    private <E> Holder<E> kilt$avoidCodecDisruptFromCrash(WritableRegistry<E> instance, ResourceKey<E> resourceKey, @Cancellable CallbackInfoReturnable<DataResult<Holder<E>>> cir) {
        var holderResult = instance.getOrCreateHolder(resourceKey);

        if (holderResult.error().isPresent()) {
            cir.setReturnValue(holderResult);
            return null;
        }

        return holderResult.result().get();
    }
}
