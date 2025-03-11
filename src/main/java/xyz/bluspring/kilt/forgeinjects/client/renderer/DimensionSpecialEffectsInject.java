package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import net.minecraftforge.client.extensions.IForgeDimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DimensionSpecialEffects.class)
public class DimensionSpecialEffectsInject implements IForgeDimensionSpecialEffects {
    @ModifyReturnValue(method = "forType", at = @At("RETURN"))
    private static DimensionSpecialEffects kilt$tryGetForType(DimensionSpecialEffects original, @Local(argsOnly = true) DimensionType dimensionType) {
        if (original == null) {
            return DimensionSpecialEffectsManager.getForType(dimensionType.effectsLocation());
        }

        return original;
    }
}
