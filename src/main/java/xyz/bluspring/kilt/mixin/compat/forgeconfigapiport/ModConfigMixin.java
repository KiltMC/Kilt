package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(value = ModConfig.class, remap = false)
public class ModConfigMixin {
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, String modId, final String fileName) {}

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod, final String fileName) {
        this(type, spec, mod.getModId(), fileName);
    }
}
