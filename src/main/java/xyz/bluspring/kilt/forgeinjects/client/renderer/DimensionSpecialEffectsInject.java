// TRACKED HASH: 870418c225798f0447483d4ac354f518addc694d
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.fabricators_of_create.porting_lib.extensions.extensions.DimensionSpecialEffectsExtensions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import net.minecraftforge.client.extensions.IForgeDimensionSpecialEffects;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = DimensionSpecialEffects.class, priority = 900)
public class DimensionSpecialEffectsInject implements IForgeDimensionSpecialEffects, DimensionSpecialEffectsExtensions {
    /**
     * @author BluSpring
     * @reason Provide Forge dimension types while also allowing other mods to mixin
     */
    @Overwrite
    public static DimensionSpecialEffects forType(DimensionType type) {
        return DimensionSpecialEffectsManager.getForType(type.effectsLocation());
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return IForgeDimensionSpecialEffects.super.renderClouds(level, ticks, partialTick, poseStack, camX, camY, camZ, projectionMatrix);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return IForgeDimensionSpecialEffects.super.renderSky(level, ticks, partialTick, poseStack, camera, projectionMatrix, isFoggy, setupFog);
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
        return IForgeDimensionSpecialEffects.super.renderSnowAndRain(level, ticks, partialTick, lightTexture, camX, camY, camZ);
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        return IForgeDimensionSpecialEffects.super.tickRain(level, ticks, camera);
    }

    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker, float skyLight, int pixelX, int pixelY, Vector3f colors) {
        IForgeDimensionSpecialEffects.super.adjustLightmapColors(level, partialTicks, skyDarken, blockLightRedFlicker, skyLight, pixelX, pixelY, colors);
    }
}