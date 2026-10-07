package xyz.bluspring.kilt.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.Registry;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.object.builder.FabricEntityDataRegistryImpl;

@Mixin(FabricEntityDataRegistryImpl.class)
public interface FabricEntityDataRegistryImplAccessor {
    @Accessor("HANDLER_REGISTRY_ID")
    static Identifier kilt$getHandlerRegistryId() {
        throw new UnsupportedOperationException();
    }

    @Accessor("handlerRegistry")
    static Registry<EntityDataSerializer<?>> kilt$getHandlerRegistry() {
        throw new UnsupportedOperationException();
    }

    @Accessor("handlerRegistry")
    static void kilt$setHandlerRegistry(Registry<EntityDataSerializer<?>> handlerRegistry) {
        throw new UnsupportedOperationException();
    }
}
