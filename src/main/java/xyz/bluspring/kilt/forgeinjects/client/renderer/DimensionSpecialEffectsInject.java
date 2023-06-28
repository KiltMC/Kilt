package xyz.bluspring.kilt.forgeinjects.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraftforge.client.extensions.IForgeDimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DimensionSpecialEffects.class)
public class DimensionSpecialEffectsInject implements IForgeDimensionSpecialEffects {
}
