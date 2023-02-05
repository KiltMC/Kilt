package xyz.bluspring.kilt.injections.world.biome;

import net.minecraft.world.level.biome.BiomeSpecialEffects;
import xyz.bluspring.kilt.mixin.GrassColorModifierAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// fuck it i'm making it necessary
public interface BiomeSpecialEffectsGrassColorModifierInjection {
    Map<String, BiomeSpecialEffects.GrassColorModifier> BY_NAME = new HashMap<>();

    static BiomeSpecialEffects.GrassColorModifier byName(String name) {
        return BY_NAME.get(name);
    }

    static BiomeSpecialEffects.GrassColorModifier create(String name, String id, ColorModifier delegate) {
        var value = EnumUtils.addEnumToClass(
                BiomeSpecialEffects.GrassColorModifier.class, GrassColorModifierAccessor.getValues(),
                name, (size) -> GrassColorModifierAccessor.createGrassColorModifier(name, size, id),
                (values) -> GrassColorModifierAccessor.setValues(values.toArray(new BiomeSpecialEffects.GrassColorModifier[0]))
        );

        ((BiomeSpecialEffectsGrassColorModifierInjection) (Object) value).setDelegate(delegate);

        return value;
    }

    @FunctionalInterface
    public interface ColorModifier {
        int modifyGrassColor(double x, double z, int color);
    }

    default void setDelegate(ColorModifier delegate) {
        throw new IllegalStateException();
    }
}
