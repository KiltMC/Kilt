package xyz.bluspring.kilt.compat.forgeconfig.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import fuzs.forgeconfigapiport.impl.network.client.NetworkHooks;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@IfModLoaded("forgeconfigapiport")
@Mixin(NetworkHooks.class)
public abstract class NetworkHooksMixin {
    /**
     * @author BluSpring
     * @reason Forcefully disable Forge Config API Port's packets
     */
    @Overwrite
    public static void handleClientLoginSuccess(Connection connection) {}
}
