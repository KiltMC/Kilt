package xyz.bluspring.kilt.injections.client.renderer;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public interface LevelRendererInjection {
    Matrix4f kilt$getProjectionMatrix();

    Frustum getFrustum();
    int getTicks();
}
