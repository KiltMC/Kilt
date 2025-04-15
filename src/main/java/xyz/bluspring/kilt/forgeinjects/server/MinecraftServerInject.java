// TRACKED HASH: 0a24b5b7aafa1b5d82426467010c877512d3e484
package xyz.bluspring.kilt.forgeinjects.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.ServerStatusPing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.server.MinecraftServerInjection;

import java.util.Map;
import java.util.Optional;

@Mixin(MinecraftServer.class)
public class MinecraftServerInject implements MinecraftServerInjection {
    private Map<ResourceKey<Level>, long[]> perWorldTickTimes = Maps.newIdentityHashMap();

    @Override
    public long[] getTickTime(ResourceKey<Level> dim) {
        return perWorldTickTimes.get(dim);
    }

    @ModifyReturnValue(method = "getServerModName", at = @At("RETURN"), remap = false)
    private String kilt$appendKiltToServerBranding(String original) {
        return original + " + kilt";
    }

    @Redirect(method = "spin", at = @At(value = "NEW", target = "java/lang/Thread"))
    private static Thread kilt$setThreadGroup(Runnable target, String name) {
        return new Thread(SidedThreadGroups.SERVER, target, name);
    }

    @Unique private static final Gson GSON = new Gson();
    @Unique private String cachedServerStatus;
    @Unique private void resetStatusCache(ServerStatus status) {
        this.cachedServerStatus = GSON.toJson(ServerStatus.CODEC.encodeStart(JsonOps.INSTANCE, status).result().orElseThrow());
    }

    @ModifyExpressionValue(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private ServerStatus kilt$resetStatusCache(ServerStatus original) {
        this.resetStatusCache(original);
        return original;
    }

    @Override
    public String getStatusJson() {
        return this.cachedServerStatus;
    }

    @ModifyReturnValue(method = "buildServerStatus", at = @At("RETURN"))
    private ServerStatus kilt$appendServerPing(ServerStatus original) {
        original.setForgeData(Optional.of(new ServerStatusPing()));
        return original;
    }

    // Tick Events implemented via Architectury
}