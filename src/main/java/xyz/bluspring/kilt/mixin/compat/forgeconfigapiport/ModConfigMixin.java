package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(value = ModConfig.class, remap = false)
public class ModConfigMixin {
    // Kilt: ForgeConfigAPIPort is missing these constructors from newer versions of Forge.

    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, String modId, final String fileName) {}
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, String modId) {}

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod, final String fileName) {
        this(type, spec, mod.getModId(), fileName);
    }

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod) {
        this(type, spec, mod.getModId(), mod.getModId());
    }
}
