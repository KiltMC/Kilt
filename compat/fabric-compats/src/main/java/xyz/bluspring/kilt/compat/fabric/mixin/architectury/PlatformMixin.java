package xyz.bluspring.kilt.compat.fabric.mixin.architectury;

import dev.architectury.platform.Platform;
import net.neoforged.api.distmarker.Dist;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.util.DistUtil;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(Platform.class)
public abstract class PlatformMixin {
    // bruh
    @CreateStatic
    private static Dist getEnv() {
        return DistUtil.INSTANCE.envTypeToDist(FabricLoader.getInstance().getEnvironmentType());
    }
}
