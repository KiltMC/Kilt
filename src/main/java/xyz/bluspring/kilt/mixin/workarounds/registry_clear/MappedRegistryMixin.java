package xyz.bluspring.kilt.mixin.workarounds.registry_clear;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.RegistrySnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.injections.core.MappedRegistryInjection;
import xyz.bluspring.kilt.loader.KiltLoader;

import java.util.HashSet;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements Registry<T>, MappedRegistryInjection<T> {

    @Unique
    private static final ResourceLocation KILT$APPLY_SNAPSHOT_PHASE = ResourceLocation.fromNamespaceAndPath(Kilt.MOD_ID, "apply_snapshot");

    @Inject(
        method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V",
        at = @At("RETURN"),
        order = 1050
    )
    private void kilt$applySnapshot(ResourceKey<? extends Registry<T>> key, Lifecycle registryLifecycle, boolean hasIntrusiveHolders, CallbackInfo ci) {
        var namespace = key.location().getNamespace();
        if (namespace.equals(ResourceLocation.DEFAULT_NAMESPACE) || KiltLoader.Companion.getInstance().hasMod(namespace)) {
            var idRemapEvent = RegistryIdRemapCallback.event(this);
            idRemapEvent.addPhaseOrdering(Event.DEFAULT_PHASE, KILT$APPLY_SNAPSHOT_PHASE);
            idRemapEvent.register(KILT$APPLY_SNAPSHOT_PHASE, state -> {
                this.unfreeze();
                var snapshot = new RegistrySnapshot(this, false);
                this.clear(false);
                snapshot.getAliases().forEach(this::addAlias);
                for (int id : snapshot.getIds().keySet()) {
                    ResourceKey<T> idKey = ResourceKey.create(key, snapshot.getIds().get(id));
                    registerIdMapping(idKey, id);
                }
                this.freeze();
            });
        }
    }

}
