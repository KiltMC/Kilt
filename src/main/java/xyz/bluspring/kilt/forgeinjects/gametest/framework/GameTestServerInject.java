package xyz.bluspring.kilt.forgeinjects.gametest.framework;

import com.mojang.datafixers.DataFixer;
import net.minecraft.gametest.framework.GameTestServer;
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

@Mixin(GameTestServer.class)
public abstract class GameTestServerInject extends MinecraftServer {
    public GameTestServerInject(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer fixerUpper, Services services, ChunkProgressListenerFactory progressListenerFactory) {
        super(serverThread, storageSource, packRepository, worldStem, proxy, fixerUpper, services, progressListenerFactory);
    }

    @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/GameTestServer;loadLevel()V"), cancellable = true)
    private void kilt$checkServerAboutToStart(CallbackInfoReturnable<Boolean> cir) {
        if (!ServerLifecycleHooks.handleServerAboutToStart(this))
            cir.setReturnValue(false);
    }
}
