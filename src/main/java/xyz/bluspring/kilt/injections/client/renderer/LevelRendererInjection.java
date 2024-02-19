package xyz.bluspring.kilt.injections.client.renderer;

import com.mojang.math.Matrix4f;

public interface LevelRendererInjection {
    Matrix4f getProjectionMatrix();
}
