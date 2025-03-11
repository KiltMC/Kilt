package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeConfigSpec.class)
public abstract class ForgeConfigSpecMixin {
    @Mixin(value = ForgeConfigSpec.ConfigValue.class, remap = false)
    public static class ConfigValueMixin {
        @Redirect(method = "get", at = @At(value = "FIELD", target = "Lnet/minecraftforge/fml/loading/FMLEnvironment;production:Z"))
        private boolean kilt$disableDevEnvCrash() {
            return true;
        }
    }
}
