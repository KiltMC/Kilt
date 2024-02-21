package xyz.bluspring.kilt.mixin.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.ClimateSettings.class)
public interface BiomeClimateSettingsAccessor {
    @Invoker("<init>")
    static Biome.ClimateSettings createClimateSettings(boolean bl, float f, Biome.TemperatureModifier temperatureModifier, float g) {
        throw new UnsupportedOperationException();
    }
}
