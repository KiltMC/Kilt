package xyz.bluspring.kilt.injections.world.level.block.state;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface BlockBehaviourPropertiesInjection {
    default Supplier<ResourceLocation> getLootTableSupplier() {
        throw new IllegalStateException();
    }
}
