package xyz.bluspring.kilt.compat.fabric.mixin.resourcefullib;

import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.teamresourceful.resourcefullib.common.registry.fabric.FabricHolderRegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.fabric.FabricRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.compat.fabric.architectury.KiltArchitecturyApiCompat;
import xyz.bluspring.kilt.loader.KiltLoader;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@IfModLoaded("resourcefullib")
@Pseudo
@Mixin(FabricHolderRegistryEntry.class)
public abstract class FabricHolderRegistryEntryMixin {
    @Inject(method = "of", at = @At("HEAD"))
    private static <T, I extends T> void kilt$ensureUnfrozenRegistry(Registry<T> registry, ResourceLocation name, Supplier<I> supplier, CallbackInfoReturnable<FabricRegistryEntry<I>> cir) {
        if (KiltLoader.Companion.getInstance().hasMod(name.getNamespace())) {
            if (registry instanceof MappedRegistry<T> mappedRegistry) // Make sure it's unfrozen so we can have intrusive holders.
                mappedRegistry.unfreeze();
        }
    }

    @WrapOperation(method = "of", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;registerForHolder(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference;"))
    private static <T, I extends T> Holder.Reference<T> kilt$delayRegisterIfNeeded(Registry<T> registry, ResourceLocation name, I value, Operation<Holder.Reference<T>> original) {
        // Defer to Kilt for registration, because deferred registry sucks.
        if (KiltLoader.Companion.getInstance().hasMod(name.getNamespace())) {
            KiltArchitecturyApiCompat.delayForRegisterEvent(registry, () -> original.call(registry, name, value));
            return registry.createIntrusiveHolder(value);
        }

        return original.call(registry, name, value);
    }
}
