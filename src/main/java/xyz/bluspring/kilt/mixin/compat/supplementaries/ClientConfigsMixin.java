package xyz.bluspring.kilt.mixin.compat.supplementaries;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@IfModLoaded("supplementaries")
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.supplementaries.configs.ClientConfigs", remap = false)
public abstract class ClientConfigsMixin {
    // Supplementaries what the FUCK
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/moonlight/api/platform/PlatformHelper;isDev()Z"), require = 0)
    private static boolean kilt$avoidServerCrashInDev() {
        return false;
    }
}
