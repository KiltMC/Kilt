package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererInject {
    @Shadow private int ticks;

    @Shadow @Nullable private Frustum capturedFrustum;

    @Shadow private Frustum cullingFrustum;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;clearRenderState()V", shift = At.Shift.BEFORE))
    public void kilt$dispatchRenderEventBasedOnType(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        var stage = RenderLevelStageEvent.Stage.fromRenderType(renderType);

        if (stage != null) {
            MinecraftForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, this.minecraft.getPartialTick(), this.minecraft.gameRenderer.getMainCamera(), this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum));
        }
    }
}
