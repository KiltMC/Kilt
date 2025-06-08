package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(value = ModConfig.class, remap = false)
public class ModConfigMixin {
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, net.fabricmc.loader.api.ModContainer container, final String fileName) {}
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, net.fabricmc.loader.api.ModContainer container) {}

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod, final String fileName) {
        this(type, spec, FabricLoader.getInstance().getModContainer(mod.getModId()).orElseThrow(), fileName);
    }

    @CreateInitializer
    public ModConfigMixin(final ModConfig.Type type, final IConfigSpec<?> spec, ModContainer mod) {
        this(type, spec, FabricLoader.getInstance().getModContainer(mod.getModId()).orElseThrow());
    }
}