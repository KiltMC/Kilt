package xyz.bluspring.kilt.injections.data.loot;

import net.minecraft.world.level.block.Block;

public interface BlockLootSubProviderInjection {
    default Iterable<Block> getKnownBlocks() {
        throw new IllegalStateException();
    }
}
