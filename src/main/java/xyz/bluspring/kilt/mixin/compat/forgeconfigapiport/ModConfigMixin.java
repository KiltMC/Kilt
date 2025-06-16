package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(value = ModConfig.class, remap = false)
public abstract class ModConfigMixin {
    // Kilt: ForgeConfigAPIPort is missing these constructors from newer versions of Forge.

    @Shadow static String defaultConfigName(ModConfig.Type type, String modId) {
        throw new IllegalStateException();
    }

    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, String modId, final String fileName) {}
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, String modId) {}

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod, final String fileName) {
        this(type, spec, mod.getModId(), fileName);
    }

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod) {
        this(type, spec, mod.getModId(), defaultConfigName(type, mod.getModId()));
    }
}
