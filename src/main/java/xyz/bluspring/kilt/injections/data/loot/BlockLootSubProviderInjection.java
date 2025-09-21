package xyz.bluspring.kilt.injections.data.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.block.Block;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(BlockLootSubProvider.class)
public interface BlockLootSubProviderInjection {
    default Iterable<Block> getKnownBlocks() {
        throw new IllegalStateException();
    }
}
