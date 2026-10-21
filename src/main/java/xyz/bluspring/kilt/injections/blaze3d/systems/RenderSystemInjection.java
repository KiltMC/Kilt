package xyz.bluspring.kilt.injections.blaze3d.systems;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.PipelineModifierStack;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.ResourceKey;

public interface RenderSystemInjection {
    PipelineModifierStack PIPELINE_MODIFIERS = new PipelineModifierStack();

    static void pushPipelineModifier(ResourceKey<PipelineModifier> modifier) {
        PIPELINE_MODIFIERS.push(modifier);
    }

    static void popPipelineModifier() {
        PIPELINE_MODIFIERS.pop();
    }

    static void renderWithPipelineModifier(ResourceKey<PipelineModifier> modifier, Runnable renderTask) {
        PIPELINE_MODIFIERS.renderWithModifier(modifier, renderTask);
    }

    @ApiStatus.Internal
    static RenderPipeline applyPipelineModifiers(RenderPipeline pipeline) {
        return PIPELINE_MODIFIERS.apply(pipeline);
    }

    @ApiStatus.Internal
    static void ensurePipelineModifiersEmpty() {
        PIPELINE_MODIFIERS.ensureEmpty();
    }
}
