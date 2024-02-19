package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.loader.api.FabricLoader;
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
import xyz.bluspring.kilt.injections.client.renderer.LevelRendererInjection;

import java.util.concurrent.atomic.AtomicReference;

// higher priority to allow for Sodium to overwrite the method
@Mixin(value = LevelRenderer.class, priority = 1050)
public class LevelRendererInject implements LevelRendererInjection {
    @Shadow private int ticks;

    @Shadow @Nullable private Frustum capturedFrustum;

    @Shadow private Frustum cullingFrustum;

    @Shadow @Final private Minecraft minecraft;

    private final AtomicReference<Matrix4f> kilt$projectionMatrix = new AtomicReference<>(null);

    @Override
    public Matrix4f getProjectionMatrix() {
        return kilt$projectionMatrix.get();
    }

    @Inject(method = "renderChunkLayer", at = @At("HEAD"))
    public void kilt$sodiumStoreProjectionMatrix(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Sodium doesn't give the projection matrix to itself, so we should store it.
        // If there's a better way of doing this, please PR.
        if (!FabricLoader.getInstance().isModLoaded("sodium"))
            return;

        this.kilt$projectionMatrix.set(projectionMatrix);
    }

    @Inject(method = "renderChunkLayer", at = @At(value = "TAIL", shift = At.Shift.BY, by = -1))
    public void kilt$dispatchRenderEventBasedOnType(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Stop right there, because Sodium injects here.
        if (FabricLoader.getInstance().isModLoaded("sodium"))
            return;

        var stage = RenderLevelStageEvent.Stage.fromRenderType(renderType);

        if (stage != null) {
            MinecraftForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, (LevelRenderer) (Object) this, poseStack, projectionMatrix, this.ticks, this.minecraft.getPartialTick(), this.minecraft.gameRenderer.getMainCamera(), this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum));
        }
    }
}
