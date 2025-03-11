// TRACKED HASH: ce9de4ebd17cd2e93a7249669656671c642e5307
package xyz.bluspring.kilt.forgeinjects.server.dedicated;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DataFixer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerInject extends MinecraftServer {
    public DedicatedServerInject(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
    }

    @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/GameProfileCache;setUsesAuthentication(Z)V", shift = At.Shift.AFTER), cancellable = true)
    public void kilt$handleServerAboutToStart(CallbackInfoReturnable<Boolean> cir) {
        if (!ServerLifecycleHooks.handleServerAboutToStart(this))
            cir.setReturnValue(false);
    }

    @ModifyReturnValue(method = "initServer", at = @At("RETURN"))
    public boolean kilt$handleServerStarting(boolean original) {
        if (original) {
            return ServerLifecycleHooks.handleServerStarting(this);
        }

        return original;
    }
}