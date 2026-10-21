package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4fc;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;

@Mixin(GameRenderer.class)
public abstract class GameRendererInject {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"))
    private void kilt$registerNeoPips(CallbackInfo ci) {
        // Kilt: We register directly into Fabric instead. Unless this causes us problems.
        ModLoader.postEvent(new RegisterPictureInPictureRenderersEvent());
    }

    @WrapWithCondition(method = "checkEntityPostEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;clearPostEffect()V"))
    private boolean kilt$checkNeoLoadShader(GameRenderer instance, @Local(name = "cameraEntity", argsOnly = true) Entity cameraEntity) {
        return !ClientHooks.loadEntityShader(cameraEntity, instance);
    }

    @Definition(id = "hurt", local = @Local(type = float.class, name = "hurt"))
    @Expression("hurt < 0.0")
    @ModifyExpressionValue(method = "bobHurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkShouldPreventFlinch(boolean original, @Local(argsOnly = true, name = "cameraState") CameraRenderState cameraState) {
        return original || cameraState.entityRenderState.kilt$preventFlinch();
    }

    @WrapOperation(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/WindowRenderState;isResized:Z", opcode = Opcodes.GETFIELD))
    private boolean kilt$checkDirectResizes(WindowRenderState instance, Operation<Boolean> original) {
        RenderTarget mainRenderTarget = this.minecraft.getMainRenderTarget();
        return original.call(instance) || instance.width != mainRenderTarget.width || instance.height != mainRenderTarget.height;
    }

    @WrapOperation(method = "extractGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    private void kilt$checkHandleLayeredGui(Screen instance, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, Operation<Void> original) {
        ClientHooks.kilt$drawScreen(instance, graphics, mouseX, mouseY, a, original::call);
    }

    @Definition(id = "profiler", local = @Local(type = ProfilerFiller.class, name = "profiler"))
    @Definition(id = "popPush", method = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V")
    @Expression("profiler.popPush('hand')")
    @Inject(method = "renderLevel", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleNeoAfterLevelRender(DeltaTracker deltaTracker, CallbackInfo ci, @Local(name = "profiler") ProfilerFiller profiler, @Local(name = "modelViewMatrix") Matrix4fc modelViewMatrix) {
        profiler.popPush("neoforge_render_after_level");
        LevelRenderer levelRenderer = this.minecraft.levelRenderer;
        NeoForge.EVENT_BUS.post(new RenderLevelStageEvent.AfterLevel(levelRenderer, levelRenderer.levelRenderState, null, modelViewMatrix, levelRenderer.getVisibleSections()));
    }
}
