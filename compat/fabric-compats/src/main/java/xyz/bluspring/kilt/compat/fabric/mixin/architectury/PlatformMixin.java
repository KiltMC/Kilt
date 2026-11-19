package xyz.bluspring.kilt.compat.fabric.mixin.architectury;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.architectury.platform.Platform;
import net.neoforged.api.distmarker.Dist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.util.DistUtil;

import net.fabricmc.loader.api.FabricLoader;

@IfModLoaded("architectury")
@Pseudo
@Mixin(Platform.class)
public abstract class PlatformMixin {
    // bruh
    @CreateStatic
    private static Dist getEnv() {
        return DistUtil.INSTANCE.envTypeToDist(FabricLoader.getInstance().getEnvironmentType());
    }
}
