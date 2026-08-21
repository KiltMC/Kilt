package xyz.bluspring.kilt.injections.world.level.block.entity;

import java.util.Set;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityTypeInjection<T extends BlockEntity> {
    default Set<Block> getValidBlocks() {
        throw KiltHelper.createMixinException(BlockEntityTypeInjection.class, "getValidBlocks");
    }

    default void kilt$setOnlyOpCanSetNbt(boolean value) {
        throw KiltHelper.createMixinException(BlockEntityInjection.class, "kilt$setOnlyOpCanSetNbt");
    }
}
