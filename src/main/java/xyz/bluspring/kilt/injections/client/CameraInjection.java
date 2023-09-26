package xyz.bluspring.kilt.injections.client;

import net.minecraft.world.level.block.state.BlockState;

public interface CameraInjection {
    void setAnglesInternal(float yaw, float pitch);
    BlockState getBlockAtCamera();
}
