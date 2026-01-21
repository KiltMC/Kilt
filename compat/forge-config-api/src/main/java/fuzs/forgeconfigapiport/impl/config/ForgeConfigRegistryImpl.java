package fuzs.forgeconfigapiport.impl.config;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import xyz.bluspring.kilt.loader.WrappedFabricModContainer;

// Copied and modified from https://github.com/Fuzss/forgeconfigapiport/blob/1.20.1/Fabric/src/main/java/fuzs/forgeconfigapiport/impl/config/ForgeConfigRegistryImpl.java
// which is licensed under the MPLv2 - https://github.com/Fuzss/forgeconfigapiport/blob/1.20.1/LICENSE.md
public final class ForgeConfigRegistryImpl implements ForgeConfigRegistry {
    @Override
    public ModConfig register(String modId, ModConfig.Type type, IConfigSpec<?> spec) {
        return new ModConfig(type, spec, new WrappedFabricModContainer(FabricLoader.getInstance().getModContainer(modId).orElseThrow()));
    }

    @Override
    public ModConfig register(String modId, ModConfig.Type type, IConfigSpec<?> spec, String fileName) {
        return new ModConfig(type, spec, new WrappedFabricModContainer(FabricLoader.getInstance().getModContainer(modId).orElseThrow()), fileName);
    }
}
