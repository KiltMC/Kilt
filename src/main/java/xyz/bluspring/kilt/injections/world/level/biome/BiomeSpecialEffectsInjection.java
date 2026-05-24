package xyz.bluspring.kilt.injections.world.level.biome;

import net.minecraft.world.level.biome.BiomeSpecialEffects;

import java.util.HashMap;
import java.util.Map;

// fuck it i'm making it necessary
public interface BiomeSpecialEffectsInjection {
    interface GrassColorModifierInjection {
        Map<String, BiomeSpecialEffects.GrassColorModifier> BY_NAME = new HashMap<>();

        static BiomeSpecialEffects.GrassColorModifier byName(String name) {
            return BY_NAME.get(name);
        }

        @FunctionalInterface
        public interface ColorModifier {
            int modifyGrassColor(double x, double z, int color);
        }

        default void setDelegate(ColorModifier delegate) {
            throw new IllegalStateException();
        }
    }
}
