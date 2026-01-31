package xyz.bluspring.kilt.injections.world.level.block.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Set;

public interface BlockEntityTypeInjection<T extends BlockEntity> {
    default Set<Block> getValidBlocks() {
        throw KiltHelper.createMixinException(BlockEntityTypeInjection.class, "getValidBlocks");
    }
}
