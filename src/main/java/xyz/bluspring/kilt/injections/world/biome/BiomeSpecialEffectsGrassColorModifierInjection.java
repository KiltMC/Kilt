package xyz.bluspring.kilt.injections.world.biome;

import net.minecraft.world.level.biome.BiomeSpecialEffects;

import java.util.Arrays;

// fuck it i'm making it necessary
public interface BiomeSpecialEffectsGrassColorModifierInjection {
    static BiomeSpecialEffects.GrassColorModifier byName(String name) {
        var modifiers = Arrays.stream(BiomeSpecialEffects.GrassColorModifier.values()).filter((it) -> it.getName().equals(name)).toList();

        if (modifiers.isEmpty())
            return null;
        else
            return modifiers.get(0);
    }
}
