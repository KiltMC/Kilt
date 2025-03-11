package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.loader.KiltModContainer;

@Mixin(value = ModConfig.class, remap = false)
public class ModConfigMixin {
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, net.fabricmc.loader.api.ModContainer container, final String fileName) {}
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, net.fabricmc.loader.api.ModContainer container) {}

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod, final String fileName) {
        this(type, spec, ((KiltModContainer) mod).getFabricModContainer(), fileName);
    }

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod) {
        this(type, spec, ((KiltModContainer) mod).getFabricModContainer());
    }
}