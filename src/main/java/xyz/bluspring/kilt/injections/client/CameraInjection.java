package xyz.bluspring.kilt.injections.client;

import net.minecraft.client.Camera;
import net.minecraft.world.level.block.state.BlockState;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(Camera.class)
public interface CameraInjection {
    default void setRotation(float yaw, float pitch, float roll) {
        throw KiltHelper.createMixinException(CameraInjection.class, "setRotation");
    }

    default float getRoll() {
        throw KiltHelper.createMixinException(CameraInjection.class, "getRoll");
    }

    default BlockState getBlockAtCamera() {
        throw KiltHelper.createMixinException(CameraInjection.class, "getBlockAtCamera");
    }
}
