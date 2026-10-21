package xyz.bluspring.kilt.injections.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.MainTarget;

public interface MainTargetInjection {
    static MainTarget create(int desiredWidth, int desiredHeight, boolean enableStencil) {
        var target = new MainTarget(desiredWidth, desiredHeight);
        target.kilt$setStencil(enableStencil);

        return target;
    }
}
