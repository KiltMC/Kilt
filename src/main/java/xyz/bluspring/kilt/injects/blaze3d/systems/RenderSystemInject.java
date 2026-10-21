package xyz.bluspring.kilt.injects.blaze3d.systems;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.client.event.FlipFrameEvent;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.PipelineModifierStack;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.blaze3d.systems.RenderSystemInjection;

import net.minecraft.resources.ResourceKey;

@Mixin(RenderSystem.class)
public abstract class RenderSystemInject implements RenderSystemInjection {
    @Unique private static final PipelineModifierStack PIPELINE_MODIFIERS = RenderSystemInjection.PIPELINE_MODIFIERS;

    @Inject(method = "flipFrame", at = @At("TAIL"))
    private static void kilt$callFlipFrameEvent(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new FlipFrameEvent());
    }

    @CreateStatic
    private static void pushPipelineModifier(ResourceKey<PipelineModifier> modifier) {
        PIPELINE_MODIFIERS.push(modifier);
    }

    @CreateStatic
    private static void popPipelineModifier() {
        PIPELINE_MODIFIERS.pop();
    }

    @CreateStatic
    private static void renderWithPipelineModifier(ResourceKey<PipelineModifier> modifier, Runnable renderTask) {
        PIPELINE_MODIFIERS.renderWithModifier(modifier, renderTask);
    }

    @ApiStatus.Internal
    @CreateStatic
    private static RenderPipeline applyPipelineModifiers(RenderPipeline pipeline) {
        return PIPELINE_MODIFIERS.apply(pipeline);
    }

    @ApiStatus.Internal
    @CreateStatic
    private static void ensurePipelineModifiersEmpty() {
        PIPELINE_MODIFIERS.ensureEmpty();
    }
}
