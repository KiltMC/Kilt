package xyz.bluspring.kilt.mixin.compat.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.renderer.LevelRendererInjection;

import java.util.concurrent.atomic.AtomicReference;

@IfModLoaded("sodium")
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements LevelRendererInjection {
    @Unique
    private final AtomicReference<Matrix4f> kilt$projectionMatrix = new AtomicReference<>(null);

    @Inject(method = "renderChunkLayer", at = @At("HEAD"))
    public void kilt$sodiumStoreProjectionMatrix(RenderType renderType, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix, CallbackInfo ci) {
        // Sodium doesn't give the projection matrix to itself, so we should store it.
        // If there's a better way of doing this, please PR.
        this.kilt$projectionMatrix.set(projectionMatrix);
    }

    @Override
    public Matrix4f kilt$getProjectionMatrix() {
        return kilt$projectionMatrix.get();
    }
}
