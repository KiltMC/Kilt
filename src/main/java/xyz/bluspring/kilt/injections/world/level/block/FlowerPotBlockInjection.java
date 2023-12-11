package xyz.bluspring.kilt.injections.world.level.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;

import java.util.Map;
import java.util.function.Supplier;

public interface FlowerPotBlockInjection {
    default FlowerPotBlock getEmptyPot() {
        throw new IllegalStateException();
    }

    default Map<ResourceLocation, Supplier<? extends Block>> getFullPotsView() {
        throw new IllegalStateException();
    }

    default void addPlant(ResourceLocation flower, Supplier<? extends Block> fullPot) {
        throw new IllegalStateException();
    }
}
