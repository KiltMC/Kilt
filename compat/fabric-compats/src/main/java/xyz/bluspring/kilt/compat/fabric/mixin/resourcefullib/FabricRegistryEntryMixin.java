package xyz.bluspring.kilt.compat.fabric.mixin.resourcefullib;

import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.teamresourceful.resourcefullib.common.registry.fabric.FabricRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.compat.fabric.architectury.KiltArchitecturyApiCompat;
import xyz.bluspring.kilt.loader.KiltLoader;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@IfModLoaded("resourcefullib")
@Pseudo
@Mixin(FabricRegistryEntry.class)
public abstract class FabricRegistryEntryMixin {
    @Inject(method = "of", at = @At("HEAD"))
    private static <T, I extends T> void kilt$ensureUnfrozenRegistry(Registry<T> registry, ResourceLocation name, Supplier<I> supplier, CallbackInfoReturnable<FabricRegistryEntry<I>> cir) {
        if (KiltLoader.Companion.getInstance().hasMod(name.getNamespace())) {
            if (registry instanceof MappedRegistry<T> mappedRegistry) // Make sure it's unfrozen so we can have intrusive holders.
                mappedRegistry.unfreeze();
        }
    }

    @WrapOperation(method = "of", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T, I extends T> T kilt$delayRegisterIfNeeded(Registry<T> registry, ResourceLocation name, I value, Operation<I> original) {
        // Defer to Kilt for registration, because deferred registry sucks.
        if (KiltLoader.Companion.getInstance().hasMod(name.getNamespace())) {
            KiltArchitecturyApiCompat.delayForRegisterEvent(registry, () -> original.call(registry, name, value));
            return value;
        }

        return original.call(registry, name, value);
    }
}
