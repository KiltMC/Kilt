package xyz.bluspring.kilt.injections.client.render;

import com.mojang.math.Matrix4f;

public interface LevelRendererInjection {
    Matrix4f getProjectionMatrix();
}
