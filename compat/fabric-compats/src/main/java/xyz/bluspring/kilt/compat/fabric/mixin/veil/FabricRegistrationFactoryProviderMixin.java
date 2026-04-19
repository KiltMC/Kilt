package xyz.bluspring.kilt.compat.fabric.mixin.veil;

import java.util.function.Supplier;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import foundry.veil.platform.registry.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@IfModLoaded("veil")
@Mixin(targets = "foundry.veil.fabric.platform.FabricRegistrationFactory$Provider")
public abstract class FabricRegistrationFactoryProviderMixin<T> {
    @Shadow
    @Final
    private Registry<T> registry;

    @Inject(method = "register", at = @At("HEAD"))
    private <I extends T> void kilt$veil$ensureUnfreezeRegistry(ResourceLocation id, Supplier<? extends I> supplier, CallbackInfoReturnable<RegistryObject<I>> cir) {
        if (this.registry instanceof MappedRegistry<T> mappedRegistry)
            mappedRegistry.unfreeze();
    }
}
