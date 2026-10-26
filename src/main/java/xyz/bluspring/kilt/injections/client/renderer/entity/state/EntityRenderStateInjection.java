package xyz.bluspring.kilt.injections.client.renderer.entity.state;

import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.BaseRenderStateWorkaround;

public interface EntityRenderStateInjection extends BaseRenderStateWorkaround {
    default float kilt$partialTick() {
        throw KiltHelper.createMixinException(EntityRenderStateInjection.class, "kilt$partialTick");
    }

    default void kilt$partialTick(float pt) {
        throw KiltHelper.createMixinException(EntityRenderStateInjection.class, "kilt$partialTick");
    }
}
