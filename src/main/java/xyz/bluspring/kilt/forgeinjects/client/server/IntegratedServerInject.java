// TRACKED HASH: 8143f2cdfb84e2696a2b99b5032125f9948928e6
package xyz.bluspring.kilt.forgeinjects.client.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerInject extends MinecraftServer {
    public IntegratedServerInject(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
    }

    @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;loadLevel()V", shift = At.Shift.BEFORE), cancellable = true)
    public void kilt$handleServerAboutToStart(CallbackInfoReturnable<Boolean> cir) {
        if (!ServerLifecycleHooks.handleServerAboutToStart(this))
            cir.setReturnValue(false);
    }

    @Inject(method = "initServer", at = @At("RETURN"), cancellable = true)
    public void kilt$handleServerStarting(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ServerLifecycleHooks.handleServerStarting(this));
    }
}