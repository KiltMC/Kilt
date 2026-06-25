package xyz.bluspring.kilt.mixin.workarounds.registry_clear;

import com.mojang.serialization.Lifecycle;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.injections.core.MappedRegistryInjection;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements Registry<T>, MappedRegistryInjection<T> {

    @Unique
    private static final Identifier KILT$APPLY_SNAPSHOT_PHASE = Identifier.fromNamespaceAndPath(Kilt.MOD_ID, "apply_snapshot");

    @Inject(
        method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V",
        at = @At("RETURN"),
        order = 1050
    )
    private void kilt$initClearBakeBridge(ResourceKey<? extends Registry<T>> key, Lifecycle registryLifecycle, boolean hasIntrusiveHolders, CallbackInfo ci) {
        var idRemapEvent = RegistryIdRemapCallback.event(this);
        idRemapEvent.addPhaseOrdering(Event.DEFAULT_PHASE, KILT$APPLY_SNAPSHOT_PHASE);
        idRemapEvent.register(KILT$APPLY_SNAPSHOT_PHASE, state -> {
            ((BaseMappedRegistry<T>) (Object) this).clearCallbacks.forEach(callback -> callback.onClear(this, false));
            ((BaseMappedRegistry<T>) (Object) this).bakeCallbacks.forEach(callback -> callback.onBake(this));
        });
    }

}
