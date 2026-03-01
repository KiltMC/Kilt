package xyz.bluspring.kilt.compat.forgeconfig.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import fuzs.forgeconfigapiport.impl.client.ForgeConfigAPIPortFabricClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@IfModLoaded("forgeconfigapiport")
@Mixin(value = ForgeConfigAPIPortFabricClient.class, remap = false)
public abstract class ForgeConfigAPIPortFabricClientMixin {
    /**
     * @author BluSpring
     * @reason Forcefully disable Forge Config API Port's initialization
     */
    @Overwrite
    public void onInitializeClient() {}
}
