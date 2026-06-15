package xyz.bluspring.kilt.compat.fabric.mixin.resourcefullib;

import java.util.function.Supplier;

import com.teamresourceful.resourcefullib.common.registry.HolderRegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.fabric.FabricResourcefulRegistry;
import com.teamresourceful.resourcefullib.common.registry.neoforge.NeoForgeHolderRegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.neoforge.NeoForgeRegistryEntry;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.api.compatibility.ModBridgeStrategy;
import xyz.bluspring.kilt.compat.fabric.resourcefullib.KiltResourcefulLibCompat;

import net.minecraft.core.Registry;

@Pseudo
@Mixin(FabricResourcefulRegistry.class)
public abstract class FabricResourcefulRegistryMixin<T> {
    @Unique private DeferredRegister<T> kilt$deferredRegister;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setupDeferredRegisterIfPossible(Registry<T> registry, String id, CallbackInfo ci) {
        if (Kilt.Companion.getLoader().hasMod(id) && !ModBridgeStrategy.checkFabricExists(id)) {
            this.kilt$deferredRegister = DeferredRegister.create(registry.key(), id);
            KiltResourcefulLibCompat.attachToModContainer(id, this.kilt$deferredRegister);
        }
    }

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private <I extends T> void kilt$tryUseNeoEntryIfPossible(String id, Supplier<I> supplier, CallbackInfoReturnable<RegistryEntry<I>> cir) {
        if (this.kilt$deferredRegister != null) {
            cir.setReturnValue(new NeoForgeRegistryEntry<>(this.kilt$deferredRegister.register(id, supplier)));
        }
    }

    @Inject(method = "registerHolder", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseNeoHolderEntryIfPossible(String id, Supplier<T> supplier, CallbackInfoReturnable<HolderRegistryEntry<T>> cir) {
        if (this.kilt$deferredRegister != null) {
            cir.setReturnValue(new NeoForgeHolderRegistryEntry<>(this.kilt$deferredRegister.register(id, supplier)));
        }
    }
}
