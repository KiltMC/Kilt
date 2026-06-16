// TRACKED HASH: 0a24b5b7aafa1b5d82426467010c877512d3e484
package xyz.bluspring.kilt.forgeinjects.server;

import java.net.Proxy;
import java.util.*;
import java.util.function.BooleanSupplier;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import net.minecraftforge.network.ServerStatusPing;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.MarkDirtyMap;
import xyz.bluspring.kilt.injections.server.MinecraftServerInjection;

import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.ServerLevelData;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerInject implements MinecraftServerInjection {
    @Shadow private MinecraftServer.ReloadableResources resources;
    @Shadow public abstract RegistryAccess.Frozen registryAccess();
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow private int tickCount;

    @Shadow @Final @Mutable private Map<ResourceKey<Level>, ServerLevel> levels;

    @Redirect(method = "spin", at = @At(value = "NEW", target = "java/lang/Thread"))
    private static Thread kilt$setThreadGroup(Runnable target, String name) {
        return new Thread(SidedThreadGroups.SERVER, target, name);
    }

    // Load Events implemented via Fabric API

    @Inject(method = "setInitialSpawn", at = @At(value = "NEW", target = "(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/ChunkPos;", ordinal = 0), cancellable = true)
    private static void kilt$checkCreateWorldSpawn(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug, CallbackInfo ci) {
        if (ForgeEventFactory.onCreateWorldSpawn(level, levelData)) {
            ci.cancel();
        }
    }

    // ForgeChunkManager::reinstatePersistentChunks handled via Porting Lib

    // Kilt: unload and server lifecycle and tick events handled via Fabric API

    @ModifyExpressionValue(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private ServerStatus kilt$resetStatusCache(ServerStatus original) {
        this.resetStatusCache(original);
        return original;
    }

    @Unique private static final Gson GSON = new Gson();
    @Unique private String cachedServerStatus;
    @Unique private void resetStatusCache(ServerStatus status) {
        this.cachedServerStatus = GSON.toJson(ServerStatus.CODEC.encodeStart(JsonOps.INSTANCE, status)
            .result().orElseThrow());
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

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kilt$prepareAutomaticDirtyMarker(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer fixerUpper, Services services, ChunkProgressListenerFactory progressListenerFactory, CallbackInfo ci) {
        levels = new MarkDirtyMap<>(levels, this::markWorldsDirty);
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void kilt$checkAutomaticDirtyMarker(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!(levels instanceof MarkDirtyMap<ResourceKey<Level>, ServerLevel>)) {
            levels = new MarkDirtyMap<>(levels, this::markWorldsDirty);
        }
    }

    @Redirect(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private Iterable<ServerLevel> kilt$tryGetAllLevelsFromArray(MinecraftServer instance) {
        return Arrays.asList(this.getWorldArray());
    }

    @Inject(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V", ordinal = 0))
    private void kilt$storeCurrentLevel(BooleanSupplier hasTimeLeft, CallbackInfo ci, @Local ServerLevel level, @Share("level") LocalRef<ServerLevel> levelRef, @Share("tickStart") LocalLongRef tickStartRef) {
        levelRef.set(level);
        tickStartRef.set(Util.getNanos());
    }

    @Inject(method = "tickChildren", slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=tick"), to = @At(value = "CONSTANT", args = "stringValue=connection")), at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 0))
    private void kilt$tryHandlePerWorldTickTimes(BooleanSupplier hasTimeLeft, CallbackInfo ci, @Share("level") LocalRef<ServerLevel> levelRef,  @Share("tickStart") LocalLongRef tickStartRef) {
        if (levelRef.get() != null) {
            this.perWorldTickTimes.computeIfAbsent(levelRef.get().dimension(), k -> new long[100])[this.tickCount % 100] = Util.getNanos() - tickStartRef.get();
        }
    }

    @ModifyExpressionValue(method = "tickChildren", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z", opcode = Opcodes.GETSTATIC))
    private boolean kilt$checkIsGameTestEnabled(boolean original) {
        return original || ForgeGameTestHooks.isGametestEnabled();
    }

    @ModifyReturnValue(method = "getServerModName", at = @At("RETURN"), remap = false)
    private String kilt$appendKiltToServerBranding(String original) {
        return original + " + kilt";
    }

    @Inject(method = "method_29440", at = @At("TAIL"))
    private void kilt$fixCommandsNotUpdating(Collection collection, MinecraftServer.ReloadableResources reloadableResources, CallbackInfo ci) {
        this.getPlayerList().getPlayers().forEach(this.getPlayerList()::sendPlayerPermissionLevel);
    }

    // Kilt TODO: do we need pack stuff?

    @Unique private Map<ResourceKey<Level>, long[]> perWorldTickTimes = Maps.newIdentityHashMap();

    @Override
    public long[] getTickTime(ResourceKey<Level> dim) {
        return perWorldTickTimes.get(dim);
    }

    @Override
    public synchronized Map<ResourceKey<Level>, ServerLevel> forgeGetWorldMap() {
        return this.levels;
    }

    @Unique
    private int worldArrayMarker = 0;
    @Unique
    private int worldArrayLast = -1;
    @Unique
    private ServerLevel[] worldArray;

    public synchronized void markWorldsDirty() {
        worldArrayMarker++;
    }

    @Unique
    private ServerLevel[] getWorldArray() {
        if (this.worldArrayMarker == this.worldArrayLast && this.worldArray != null)
            return this.worldArray;

        this.worldArray = this.levels.values().toArray(new ServerLevel[0]);
        this.worldArrayLast = this.worldArrayMarker;
        return this.worldArray;
    }

    @Override
    public MinecraftServer.ReloadableResources getServerResources() {
        return this.resources;
    }

    // Tick Events implemented via Fabric API
}
