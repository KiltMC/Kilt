// TRACKED HASH: 8143f2cdfb84e2696a2b99b5032125f9948928e6
package xyz.bluspring.kilt.injects.client.server;

import java.net.Proxy;
import java.util.Optional;

import com.mojang.datafixers.DataFixer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerInject extends MinecraftServer {
    public IntegratedServerInject(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Optional<GameRules> gameRules, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener, boolean propagatesCrashes) {
        super(serverThread, storageSource, packRepository, worldStem, gameRules, proxy, fixerUpper, services, levelLoadListener, propagatesCrashes);
    }

    @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;loadLevel()V", shift = At.Shift.BEFORE))
    public void kilt$handleServerAboutToStart(CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleHooks.handleServerAboutToStart(this);
    }

    @Inject(method = "initServer", at = @At("RETURN"))
    public void kilt$handleServerStarting(CallbackInfoReturnable<Boolean> cir) {
        ServerLifecycleHooks.handleServerStarting(this);
    }
}
