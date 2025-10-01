// TRACKED HASH: 870418c225798f0447483d4ac354f518addc694d
package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.client.DimensionSpecialEffectsManager;
import net.neoforged.neoforge.client.extensions.IDimensionSpecialEffectsExtension;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DimensionSpecialEffects.class, priority = 900)
public abstract class DimensionSpecialEffectsInject implements IDimensionSpecialEffectsExtension {
    @ModifyReturnValue(method = "forType", at = @At("RETURN"))
    private static DimensionSpecialEffects kilt$tryUseForgeSpecialEffects(DimensionSpecialEffects original, @Local(argsOnly = true) DimensionType type) {
        var effects = DimensionSpecialEffectsManager.getForType(type.effectsLocation());

        // If dimension special effects are the default in Forge, use the Vanilla one.
        if (effects.equals(DimensionSpecialEffectsManager.kilt$getDefaultEffects())) {
            return original;
        }

        return effects;
    }
}