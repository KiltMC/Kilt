// TRACKED HASH: ce9de4ebd17cd2e93a7249669656671c642e5307
package xyz.bluspring.kilt.injects.server.dedicated;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerInject extends MinecraftServer {
    public DedicatedServerInject(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
    }

    @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/GameProfileCache;setUsesAuthentication(Z)V", shift = At.Shift.AFTER), cancellable = true)
    public void kilt$handleServerAboutToStart(CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleHooks.handleServerAboutToStart(this);
    }

    @Inject(method = "initServer", at = @At("TAIL"))
    public void kilt$handleServerStarting(CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleHooks.handleServerStarting(this);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void kilt$handleGameShuttingDownEvent(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new GameShuttingDownEvent());
    }
}