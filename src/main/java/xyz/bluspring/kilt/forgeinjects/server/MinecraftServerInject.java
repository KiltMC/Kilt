// TRACKED HASH: 0a24b5b7aafa1b5d82426467010c877512d3e484
package xyz.bluspring.kilt.forgeinjects.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BooleanSupplier;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.MinecraftServerInjection;
import xyz.bluspring.kilt.injections.world.item.alchemy.PotionBrewingInjection;

import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerInject implements MinecraftServerInjection {
    @Shadow private MinecraftServer.ReloadableResources resources;
    @Shadow public abstract RegistryAccess.Frozen registryAccess();
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow private int tickCount;

    @Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Redirect(method = "spin", at = @At(value = "NEW", target = "java/lang/Thread"))
    private static Thread kilt$setThreadGroup(Runnable target, String name) {
        return new Thread(SidedThreadGroups.SERVER, target, name);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionBrewing;bootstrap(Lnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/world/item/alchemy/PotionBrewing;"))
    private PotionBrewing kilt$addRegistryAccessToBrewingBootstrap(FeatureFlagSet enabledFeatures, Operation<PotionBrewing> original) {
        try {
            PotionBrewingInjection.kilt$registryAccess.set(this.registryAccess());
            return original.call(enabledFeatures);
        } finally {
            PotionBrewingInjection.kilt$registryAccess.set(RegistryAccess.EMPTY);
        }
    }

    // Load Events implemented via Fabric API

    @Inject(method = "setInitialSpawn", at = @At(value = "NEW", target = "(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/ChunkPos;", ordinal = 0), cancellable = true)
    private static void kilt$checkCreateWorldSpawn(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug, CallbackInfo ci) {
        if (EventHooks.onCreateWorldSpawn(level, levelData)) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "prepareLevels", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/LongIterator;hasNext()Z"))
    private boolean kilt$tryReinstatePersistentChunks(boolean original, @Local(ordinal = 1) ServerLevel level, @Local ForcedChunksSavedData savedData) {
        if (!original) {
            ForcedChunkManager.reinstatePersistentChunks(level, savedData);
        }

        return original;
    }

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
    
    @ModifyReturnValue(method = "buildServerStatus", at = @At("RETURN"))
    private ServerStatus kilt$applyModdedServerToStatus(ServerStatus original) {
        original.kilt$setModded(true);
        return original;
    }

    @WrapOperation(method = "tickChildren", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private Iterable<ServerLevel> kilt$tryGetAllLevelsFromArray(MinecraftServer instance, Operation<Iterable<ServerLevel>> original) {
        if (this.worldArrayLast != -1) { // Kilt: This path will only appear if a mod calls markWorldsDirty once at some point.
            return Arrays.asList(this.getWorldArray());
        }

        return original.call(instance);
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
        return original || GameTestHooks.isGametestEnabled();
    }

    @WrapOperation(method = "synchronizeTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/resources/ResourceKey;)V"))
    private void kilt$trySynchronizeWithNeoPacket(PlayerList instance, Packet<?> packet, ResourceKey<Level> dimension, Operation<Void> original, @Local(argsOnly = true) ServerLevel level) {
        if (packet instanceof ClientboundSetTimePacket originalPacket) {
            var neoPacket = new ClientboundCustomSetTimePayload(originalPacket.getGameTime(), originalPacket.getDayTime(), level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT), level.getDayTimeFraction(), level.getDayTimePerTick());
            for (ServerPlayer player : this.getPlayerList().getPlayers()) {
                if (player.level().dimension() == dimension) {
                    if (player.connection.hasChannel(ClientboundCustomSetTimePayload.TYPE)) {
                        player.connection.send(neoPacket);
                    } else {
                        player.connection.send(originalPacket);
                    }
                }
            }
        } else {
            original.call(instance, packet, dimension);
        }
    }

    @ModifyReturnValue(method = "getServerModName", at = @At("RETURN"), remap = false)
    private String kilt$appendKiltToServerBranding(String original) {
        return original + " + kilt";
    }

    @ModifyReceiver(method = "method_29442", at = @At(value = "INVOKE", target = "Ljava/util/Collection;stream()Ljava/util/stream/Stream;"))
    private Collection<?> kilt$tryRebuildSelected(Collection<?> instance) {
        // Kilt TODO: do we need this?
        return instance;
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

    public synchronized Map<ResourceKey<Level>, ServerLevel> forgeGetWorldMap() {
        return this.levels;
    }

    @Unique private int worldArrayMarker = 0;
    @Unique private int worldArrayLast = -1;
    @Unique private ServerLevel[] worldArray;

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
