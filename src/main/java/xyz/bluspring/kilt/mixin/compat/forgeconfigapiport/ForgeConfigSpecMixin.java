package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeConfigSpec.class, remap = false)
public abstract class ForgeConfigSpecMixin {
    @Mixin(value = ForgeConfigSpec.ConfigValue.class, remap = false)
    public static class ConfigValueMixin {
        @Redirect(method = "get", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Preconditions;checkState(ZLjava/lang/Object;)V"))
        private void kilt$disableDevEnvCrash(boolean expression, Object errorMessage) {
        }
    }
}
