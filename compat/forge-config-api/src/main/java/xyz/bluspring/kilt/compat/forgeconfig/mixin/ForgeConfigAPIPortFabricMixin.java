package xyz.bluspring.kilt.compat.forgeconfig.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import fuzs.forgeconfigapiport.impl.ForgeConfigAPIPortFabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@IfModLoaded("forgeconfigapiport")
@Mixin(ForgeConfigAPIPortFabric.class)
public abstract class ForgeConfigAPIPortFabricMixin {
    /**
     * @author BluSpring
     * @reason Forcefully disable Forge Config API Port's initialization
     */
    @Overwrite
    public void onInitialize() {}
}
