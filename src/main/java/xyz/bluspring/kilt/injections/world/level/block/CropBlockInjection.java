package xyz.bluspring.kilt.injections.world.level.block;

import net.minecraft.world.level.block.state.BlockState;

public interface CropBlockInjection {
    ThreadLocal<BlockState> kilt$currentState = new ThreadLocal<>();
}
