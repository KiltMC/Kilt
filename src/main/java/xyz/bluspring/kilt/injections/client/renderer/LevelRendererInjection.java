package xyz.bluspring.kilt.injections.client.renderer;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Consumer;

public interface LevelRendererInjection {
    default Frustum getFrustum() {
        throw KiltHelper.createMixinException(LevelRendererInjection.class, "getFrustum");
    }

    default int getTicks() {
        throw KiltHelper.createMixinException(LevelRendererInjection.class, "getTicks");
    }

    default void iterateVisibleBlockEntities(Consumer<BlockEntity> blockEntityConsumer) {
        throw KiltHelper.createMixinException(LevelRendererInjection.class, "blockEntityConsumer");
    }

    default void requestOutlineEffect() {
        throw KiltHelper.createMixinException(LevelRendererInjection.class, "requestOutlineEffect");
    }
}
