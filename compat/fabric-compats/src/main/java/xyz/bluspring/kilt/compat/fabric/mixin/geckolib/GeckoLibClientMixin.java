package xyz.bluspring.kilt.compat.fabric.mixin.geckolib;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.GeckoLibClient;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@IfModLoaded("geckolib")
@Mixin(GeckoLibClient.class)
public abstract class GeckoLibClientMixin {
    @CreateStatic
    private static void init() {
        // This isn't API, why do people do this?
    }
}
