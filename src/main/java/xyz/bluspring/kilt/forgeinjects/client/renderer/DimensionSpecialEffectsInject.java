// TRACKED HASH: 870418c225798f0447483d4ac354f518addc694d
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import net.minecraftforge.client.extensions.IForgeDimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = DimensionSpecialEffects.class, priority = 900)
public class DimensionSpecialEffectsInject implements IForgeDimensionSpecialEffects {
    /**
     * @author BluSpring
     * @reason Provide Forge dimension types while also allowing other mods to mixin
     */
    @Overwrite
    public static DimensionSpecialEffects forType(DimensionType type) {
        return DimensionSpecialEffectsManager.getForType(type.effectsLocation());
    }
}