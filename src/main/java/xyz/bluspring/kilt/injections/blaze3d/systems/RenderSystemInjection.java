package xyz.bluspring.kilt.injections.blaze3d.systems;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import org.jetbrains.annotations.ApiStatus;
import xyz.bluspring.kilt.helpers.FabricImplemented;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.ResourceKey;

@FabricImplemented(RenderSystem.class)
public interface RenderSystemInjection {
    static void pushPipelineModifier(ResourceKey<PipelineModifier> modifier) {
        throw KiltHelper.createMixinException(RenderSystemInjection.class, "pushPipelineModifier");
    }

    static void popPipelineModifier() {
        throw KiltHelper.createMixinException(RenderSystemInjection.class, "popPipelineModifier");
    }

    static void renderWithPipelineModifier(ResourceKey<PipelineModifier> modifier, Runnable renderTask) {
        throw KiltHelper.createMixinException(RenderSystemInjection.class, "renderWithPipelineModifier");
    }

    @ApiStatus.Internal
    static RenderPipeline applyPipelineModifiers(RenderPipeline pipeline) {
        throw KiltHelper.createMixinException(RenderSystemInjection.class, "applyPipelineModifiers");
    }

    @ApiStatus.Internal
    static void ensurePipelineModifiersEmpty() {
        throw KiltHelper.createMixinException(RenderSystemInjection.class, "ensurePipelineModifiersEmpty");
    }
}
