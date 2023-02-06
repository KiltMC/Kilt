package xyz.bluspring.kilt.mixin;

import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BiomeSpecialEffects.GrassColorModifier.class)
public interface GrassColorModifierAccessor {
    @Invoker("<init>")
    static BiomeSpecialEffects.GrassColorModifier createGrassColorModifier(String name, int id, String string2) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static BiomeSpecialEffects.GrassColorModifier[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(BiomeSpecialEffects.GrassColorModifier[] values) {
        throw new IllegalStateException();
    }
}
