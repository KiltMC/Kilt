package xyz.bluspring.kilt.remaps.world.level.biome

import net.minecraft.world.level.biome.BiomeSpecialEffects

// technically this is actually BiomeSpecialEffectsGrassColorModifierRemap, but it's so long. so unnecessary.
object BiomeSpecialEffectsRemap {
    @JvmStatic
    fun byName(name: String): BiomeSpecialEffects.GrassColorModifier? {
        return BiomeSpecialEffects.GrassColorModifier.values().firstOrNull {
            it.getName() == name
        }
    }
}