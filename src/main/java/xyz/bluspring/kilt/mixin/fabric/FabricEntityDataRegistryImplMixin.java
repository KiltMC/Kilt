package xyz.bluspring.kilt.mixin.fabric;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.impl.object.builder.FabricEntityDataRegistryImpl;

@Mixin(FabricEntityDataRegistryImpl.class)
public abstract class FabricEntityDataRegistryImplMixin {
    @Shadow private static @Nullable Registry<EntityDataSerializer<?>> handlerRegistry;
    @Shadow @Final private static ResourceKey<Registry<EntityDataSerializer<?>>> HANDLER_REGISTRY_KEY;

    @Shadow
    private static void storeExternalHandlers() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static void reorderHandlers() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$fabric_api$alwaysInitFabricRegistry(CallbackInfo ci) {
        // Kilt: We want to make sure Fabric's registry handlers always exist, because otherwise NeoForge panics.
        if (handlerRegistry == null) {
            handlerRegistry = FabricRegistryBuilder
                .create(HANDLER_REGISTRY_KEY)
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();

            RegistryIdRemapCallback.event(handlerRegistry).register(state -> {
                storeExternalHandlers();
                reorderHandlers();
            });
        }
    }
}
