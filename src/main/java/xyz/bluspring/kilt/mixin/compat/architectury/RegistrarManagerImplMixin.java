package xyz.bluspring.kilt.mixin.compat.architectury;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.architectury.registry.registries.fabric.RegistrarManagerImpl;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("architectury")
@Pseudo
@Mixin(RegistrarManagerImpl.RegistrarImpl.class)
public class RegistrarManagerImplMixin<T> {
    @Shadow private Registry<T> delegate;

    // FIXME: This is very much a workaround but idk how else to fix this.
    //        The main issue is that Kilt is somehow preventing Fabric's RegistryEntryAdded callback from running, which causes issues with Architectury's spawn egg items especially.
    @ModifyExpressionValue(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;"))
    private T kilt$forceAddRegister(T original) {
        RegistryEntryAddedCallback.event(delegate).invoker().onEntryAdded(delegate.getId(original), delegate.getKey(original), original);
        return original;
    }
}
