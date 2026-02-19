package xyz.bluspring.kilt.compat.forgeconfig.mixin;

import com.mojang.datafixers.util.Pair;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import fuzs.forgeconfigapiport.impl.network.config.ConfigSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collections;
import java.util.List;

@IfModLoaded("forgeconfigapiport")
@Mixin(value = ConfigSync.class, remap = false)
public abstract class ConfigSyncMixin {
    /**
     * @author BluSpring
     * @reason Prevent Forge Config API Port from handling things.
     */
    @Overwrite
    public static void unloadSyncedConfig() {
    }
}
