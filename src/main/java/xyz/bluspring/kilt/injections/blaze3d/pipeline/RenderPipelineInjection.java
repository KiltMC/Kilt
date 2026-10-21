package xyz.bluspring.kilt.injections.blaze3d.pipeline;

import java.util.Optional;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.neoforged.neoforge.client.stencil.StencilTest;
import xyz.bluspring.kilt.util.KiltHelper;

public interface RenderPipelineInjection {
    default Optional<StencilTest> getStencilTest() {
        throw KiltHelper.createMixinException(RenderPipelineInjection.class, "getStencilTest");
    }

    default void kilt$setStencilTest(Optional<StencilTest> stencilTest) {
        throw KiltHelper.createMixinException(RenderPipelineInjection.class, "kilt$setStencilTest");
    }

    default RenderPipeline.Builder toBuilder() {
        throw KiltHelper.createMixinException(RenderPipelineInjection.class, "toBuilder");
    }

    interface BuilderInjection {
        default RenderPipeline.Builder withStencilTest(StencilTest stencilTest) {
            throw KiltHelper.createMixinException(BuilderInjection.class, "withStencilTest");
        }

        default RenderPipeline.Builder withoutStencilTest() {
            throw KiltHelper.createMixinException(BuilderInjection.class, "withoutStencilTest");
        }
    }

    interface SnippetInjection {
        default Optional<StencilTest> stencilTest() {
            throw KiltHelper.createMixinException(SnippetInjection.class, "stencilTest");
        }
    }
}
