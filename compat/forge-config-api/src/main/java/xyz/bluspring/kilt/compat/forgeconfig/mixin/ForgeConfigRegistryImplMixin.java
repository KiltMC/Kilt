package xyz.bluspring.kilt.compat.forgeconfig.mixin;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.forgeconfigapiport.impl.config.ForgeConfigRegistryImpl;
import fuzs.forgeconfigapiport.impl.core.CommonAbstractions;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeConfigRegistryImpl.class, remap = false)
public abstract class ForgeConfigRegistryImplMixin {
    @ModifyReturnValue(method = "register*", at = @At("RETURN"))
    private ModConfig kilt$fcap$loadTrackedConfig(ModConfig original) {
        kilt$loadTrackedConfig(original);
        return original;
    }

    @Unique
    private static void kilt$loadTrackedConfig(ModConfig config) {
        if (config.getType() == ModConfig.Type.CLIENT) {
            kilt$openConfig(config, CommonAbstractions.INSTANCE.getClientConfigDirectory());
        } else if (config.getType() == ModConfig.Type.COMMON) {
            kilt$openConfig(config, CommonAbstractions.INSTANCE.getCommonConfigDirectory());
        }
    }

    @Unique
    private static void kilt$openConfig(ModConfig config, Path configBasePath) {
        final CommentedFileConfig configData = config.getHandler().reader(configBasePath).apply(config);
        config.setConfigData(configData);

        // Forge Config API Port: invoke Fabric style callback instead of Forge event
        CommonAbstractions.INSTANCE.fireConfigLoading(config.getModId(), config);
        config.save();
    }
}
