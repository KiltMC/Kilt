package xyz.bluspring.kilt.injects.server.level;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.LevelAttachmentsSavedData;
import net.neoforged.neoforge.capabilities.CapabilityListenerHolder;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.ClockAdjustment;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.level.ServerLevelInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerLevel.class)
public abstract class ServerLevelInject extends Level implements ServerLevelInjection {
    protected ServerLevelInject(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow @Final @Mutable private List<CustomSpawner> customSpawners;

    @Unique final Int2ObjectMap<PartEntity<?>> kilt$entityParts = new Int2ObjectOpenHashMap<>();
    @Unique private OptionalLong seedOverride = OptionalLong.empty();

    @Override
    public OptionalLong getSeedOverride() {
        return this.seedOverride;
    }

    // Kilt TODO: pain.
//    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;<init>(Lnet/minecraft/world/level/storage/WritableLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/core/Holder;ZZJI)V"))
//    private static long kilt$useCustomBiomeZoomSeed(long seed, @Local(argsOnly = true, name = "levelStem") LevelStem levelStem) {
//        return ExtraCodecs.fromOptionalLong.apply(levelStem.seedOverride())
//            .map(BiomeManager::obfuscateSeed)
//            .orElse(seed);
//    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$addInitCapabilities(MinecraftServer server, Executor executor, LevelStorageSource.LevelStorageAccess levelStorage, ServerLevelData levelData, ResourceKey dimension, LevelStem levelStem, boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime, CallbackInfo ci) {
        LevelAttachmentsSavedData.init((ServerLevel) (Object) this);
        this.customSpawners = EventHooks.getCustomSpawners((ServerLevel) (Object) this, this.customSpawners);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/clock/ServerClockManager;moveToTimeMarker(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceKey;)Z"))
    public boolean kilt$useNeoSleepFinish(ServerClockManager instance, Holder<WorldClock> clock, ResourceKey<ClockTimeMarker> timeMarkerId, Operation<Boolean> original) {
        var marker = new ClockAdjustment.Marker(timeMarkerId);
        var adjustment = EventHooks.onSleepFinished((ServerLevel) (Object) this, marker);
        if (adjustment != null && adjustment != marker) {
            adjustment.apply(instance, clock);
        } else if (adjustment == null) {
            return false;
        }

        return original.call(instance, clock, timeMarkerId);
    }

    @Definition(id = "hasActiveTickets", method = "Lnet/minecraft/server/level/ServerChunkCache;hasActiveTickets()Z")
    @Definition(id = "chunkSource", field = "Lnet/minecraft/server/level/ServerLevel;chunkSource:Lnet/minecraft/server/level/ServerChunkCache;")
    @Expression("this.chunkSource.hasActiveTickets()")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkHasForcedChunks(boolean original) {
        return original || ForcedChunkManager.hasForcedChunks((ServerLevel) (Object) this);
    }

    @WrapWithCondition(method = "lambda$tick$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/Entity;)V"))
    private boolean kilt$checkValidTickingEntity(ServerLevel instance, Consumer consumer, Entity entity) {
        return !entity.isRemoved() && !(entity instanceof PartEntity<?>);
    }

    @WrapOperation(method = "tickPrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$checkAreaLoaded(Biome instance, LevelReader level, BlockPos pos, Operation<Boolean> original) {
        return this.isAreaLoaded(pos, 1) && original.call(instance, level, pos);
    }

    @Redirect(method = "advanceWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"), require = 0)
    private void kilt$limitToCurrentDimension(PlayerList instance, Packet<?> packet) {
        instance.broadcastAll(packet, this.dimension());
    }

    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void kilt$callEntityTickEvents(Entity instance, Operation<Void> original) {
        if (!EventHooks.fireEntityTickPre(instance).isCanceled()) {
            original.call(instance);
            EventHooks.fireEntityTickPost(instance);
        }
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void kilt$handleSaveEvent(ProgressListener progress, boolean flush, boolean skipSave, CallbackInfo ci) {
        if (!skipSave) {
            NeoForge.EVENT_BUS.post(new LevelEvent.Save(this));
        }
    }

    @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
    private void kilt$callEntityJoinLevelEvent(ServerPlayer player, CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new EntityJoinLevelEvent(player, this)).isCanceled()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "addPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;addNewEntity(Lnet/minecraft/world/level/entity/EntityAccess;)Z"))
    private <T extends EntityAccess> boolean kilt$markAddWithoutEvent(PersistentEntitySectionManager<T> instance, T entityAccess, Operation<Boolean> original) {
        instance.kilt$markWithoutEvent();
        return original.call(instance, entityAccess);
    }

    @Inject(method = "addPlayer", at = @At("TAIL"))
    private void kilt$addEntityToWorld(ServerPlayer player, CallbackInfo ci) {
        player.onAddedToLevel();
    }

    @WrapOperation(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;addNewEntity(Lnet/minecraft/world/level/entity/EntityAccess;)Z"))
    private <T extends EntityAccess> boolean kilt$checkEntityAddToWorld(PersistentEntitySectionManager<T> instance, T entity, Operation<Boolean> original) {
        if (original.call(instance, entity)) {
            ((Entity) entity).onAddedToLevel();
            return true;
        } else {
            return false;
        }
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$modifyPositionedSound(CallbackInfo ci, @Local(argsOnly = true, name = "x") double x, @Local(argsOnly = true, name = "y") double y, @Local(argsOnly = true, name = "z") double z, @Local(argsOnly = true, name = "sound") LocalRef<Holder<SoundEvent>> sound, @Local(argsOnly = true, name = "source") LocalRef<SoundSource> source, @Local(argsOnly = true, name = "volume") LocalFloatRef volume, @Local(argsOnly = true, name = "pitch") LocalFloatRef pitch) {
        var event = EventHooks.onPlaySoundAtPosition(this, x, y, z, sound.get(), source.get(), volume.get(), pitch.get());

        if (event.isCanceled() || event.getSound() == null) {
            ci.cancel();
            return;
        }

        sound.set(event.getSound());
        source.set(event.getSource());
        volume.set(event.getNewVolume());
        pitch.set(event.getNewPitch());
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$modifyEntitySound(CallbackInfo ci, @Local(argsOnly = true, name = "sourceEntity") Entity entity, @Local(argsOnly = true, name = "sound") LocalRef<Holder<SoundEvent>> sound, @Local(argsOnly = true, name = "source") LocalRef<SoundSource> source, @Local(argsOnly = true, name = "volume") LocalFloatRef volume, @Local(argsOnly = true, name = "pitch") LocalFloatRef pitch) {
        var event = EventHooks.onPlaySoundAtEntity(entity, sound.get(), source.get(), volume.get(), pitch.get());

        if (event.isCanceled() || event.getSound() == null) {
            ci.cancel();
            return;
        }

        sound.set(event.getSound());
        source.set(event.getSource());
        volume.set(event.getNewVolume());
        pitch.set(event.getNewPitch());
    }

    @Inject(method = "gameEvent", at = @At("HEAD"), cancellable = true)
    private void kilt$callVanillaGameEvent(Holder<GameEvent> event, Vec3 position, GameEvent.Context context, CallbackInfo ci) {
        if (!CommonHooks.onVanillaGameEvent(this, event, position, context))
            ci.cancel();
    }

    @Inject(method = "updateNeighborsAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V", at = @At("HEAD"))
    private void kilt$notifyNeighborsEvent(BlockPos pos, Block sourceBlock, Orientation orientation, CallbackInfo ci) {
        EventHooks.onNeighborNotify(this, pos, this.getBlockState(pos), EnumSet.allOf(Direction.class), false)
            .isCanceled(); // TODO: what's this for? why is this in Forge's patch? // 2026 update: it's still there, why?
    }

    @Inject(method = "updateNeighborsAtExceptFromFacing", at = @At("HEAD"), cancellable = true)
    private void kilt$notifyNeighborsEventWithoutFacing(BlockPos pos, Block blockObject, Direction skipDirection, Orientation orientation, CallbackInfo ci) {
        var directions = EnumSet.allOf(Direction.class);
        directions.remove(skipDirection);

        if (EventHooks.onNeighborNotify(this, pos, this.getBlockState(pos), directions, false).isCanceled())
            ci.cancel();
    }

    @Override
    public Int2ObjectMap<PartEntity<?>> kilt$getEntityParts() {
        return this.kilt$entityParts;
    }

    @Override
    public Collection<PartEntity<?>> kilt$getPartEntities() {
        return this.kilt$entityParts.values();
    }

    @Override
    public void syncData(AttachmentType<?> type) {
        AttachmentSync.syncLevelUpdate((ServerLevel) (Object) this, type);
    }

    @Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
    public static abstract class EntityCallbacksInject implements LevelCallback<Entity> {
        @Shadow @Final
        ServerLevel this$0;

        @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
        @Definition(id = "EnderDragon", type = EnderDragon.class)
        @Expression("entity instanceof EnderDragon")
        @WrapOperation(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$startTrackingMultipart(Object object, Operation<Boolean> original) {
            if (original.call(object))
                return true;

            if (((Entity) object).isMultipartEntity()) {
                var parts = ((Entity) object).getParts();

                if (parts != null) {
                    for (PartEntity<?> part : parts) {
                        this$0.kilt$getEntityParts().put(part.getId(), part);
                    }
                }
            }

            return false;
        }

        @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
        @Definition(id = "EnderDragon", type = EnderDragon.class)
        @Expression("entity instanceof EnderDragon")
        @WrapOperation(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$stopTrackingMultipart(Object object, Operation<Boolean> original) {
            if (original.call(object))
                return true;

            if (((Entity) object).isMultipartEntity()) {
                var parts = ((Entity) object).getParts();

                if (parts != null) {
                    for (PartEntity<?> part : parts) {
                        this$0.kilt$getEntityParts().remove(part.getId());
                    }
                }
            }

            return false;
        }

        @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
        private void kilt$callEntityLevelRemoveEvent(Entity entity, CallbackInfo ci) {
            entity.onRemovedFromLevel();
            NeoForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, this$0));
        }
    }

    @Unique private final CapabilityListenerHolder capListenerHolder = new CapabilityListenerHolder();

    @Override
    public void invalidateCapabilities(BlockPos pos) {
        this.capListenerHolder.invalidatePos(pos);
    }

    @Override
    public void invalidateCapabilities(ChunkPos pos) {
        this.capListenerHolder.invalidateChunk(pos);
    }

    @Override
    public void registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        this.capListenerHolder.addListener(pos, listener);
    }

    @Override
    public void cleanCapabilityListenerReferences() {
        this.capListenerHolder.clean();
    }

}
