package xyz.bluspring.kilt.injections.client.renderer;

import org.joml.Matrix4f;

public interface LevelRendererInjection {
    Matrix4f getProjectionMatrix();
}
