package xyz.bluspring.kilt.injects.world.level.biome;

import net.neoforged.fml.common.asm.enumextension.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.AbstractOverride;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.biome.BiomeSpecialEffectsInjection;

import net.minecraft.world.level.biome.BiomeSpecialEffects;

@Mixin(BiomeSpecialEffects.class)
public abstract class BiomeSpecialEffectsInject {
    @NamedEnum
    @NetworkedEnum(NetworkedEnum.NetworkCheck.CLIENTBOUND)
    @Mixin(BiomeSpecialEffects.GrassColorModifier.class)
    public abstract static class GrassColorModifierInject implements IExtensibleEnum, BiomeSpecialEffectsInjection.GrassColorModifierInjection {
        @Unique private ColorModifier delegate;

        @AbstractOverride
        public int modifyColor(double x, double z, int grassColor) {
            return this.delegate.modifyGrassColor(x, z, grassColor);
        }

        @ReservedConstructor
        GrassColorModifierInject(String fieldName, int ordinal, String name) {}

        @CreateInitializer
        GrassColorModifierInject(String fieldName, int ordinal, String name, ColorModifier delegate) {
            this(fieldName, ordinal, name);
            this.delegate = delegate;
        }

        @Override
        public void setDelegate(ColorModifier delegate) {
            this.delegate = delegate;
        }

        public ColorModifier kilt$getDelegate() {
            return this.delegate;
        }

        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended(BiomeSpecialEffects.GrassColorModifier.class);
        }
    }
}
