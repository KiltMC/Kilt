package xyz.bluspring.kilt.injections.world.level.block.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public interface BlockBehaviour$PropertiesInjection {
    default Supplier<ResourceLocation> getLootTableSupplier() {
        throw new IllegalStateException();
    }
    default BlockBehaviour.Properties lootFrom(Supplier<? extends Block> blockIn) {
        throw new IllegalStateException();
    }
}
