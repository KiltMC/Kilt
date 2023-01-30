package xyz.bluspring.kilt.forgeinjects.world.level.biome;

import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.biome.BiomeSpecialEffectsGrassColorModifierInjection;

@Mixin(BiomeSpecialEffects.GrassColorModifier.class)
// such a long line for just a class, bruh
public class BiomeSpecialEffectsGrassColorModifierInject implements BiomeSpecialEffectsGrassColorModifierInjection {
}
