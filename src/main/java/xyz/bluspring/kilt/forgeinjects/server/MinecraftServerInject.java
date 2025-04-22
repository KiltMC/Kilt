package xyz.bluspring.kilt.forgeinjects.server;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.MinecraftServerInjection;

import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerInject implements MinecraftServerInjection {
    @Shadow private MinecraftServer.ReloadableResources resources;
    @Shadow @Final private ServerStatus status;
    private Map<ResourceKey<Level>, long[]> perWorldTickTimes = Maps.newIdentityHashMap();

    @Override
    public long[] getTickTime(ResourceKey<Level> dim) {
        return perWorldTickTimes.get(dim);
    }

    @Redirect(method = "spin", at = @At(value = "NEW", target = "java/lang/Thread"))
    private static Thread kilt$setThreadGroup(Runnable target, String name) {
        return new Thread(SidedThreadGroups.SERVER, target, name);
    }

    @ModifyReturnValue(method = "getServerModName", at = @At("RETURN"), remap = false)
    private String kilt$appendKiltToServerBranding(String original) {
        return original + " + kilt";
    }

    @Override
    public MinecraftServer.ReloadableResources getServerResources() {
        return this.resources;
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/status/ServerStatus$Players;setSample([Lcom/mojang/authlib/GameProfile;)V", shift = At.Shift.AFTER))
    private void kilt$resetStatusCache(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.status.invalidateJson();
    }

    // Tick Events implemented via Architectury
}
