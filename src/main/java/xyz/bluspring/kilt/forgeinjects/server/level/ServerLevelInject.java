package xyz.bluspring.kilt.forgeinjects.server.level;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.minecraftforge.common.util.LevelCapabilityData;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ServerLevelInjection;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelInject extends Level implements IForgeLevel, ServerLevelInjection {
    @Unique final Int2ObjectMap<PartEntity<?>> kilt$entityParts = new Int2ObjectOpenHashMap<>();
    @Shadow public abstract DimensionDataStorage getDataStorage();

    @Unique
    private LevelCapabilityData capabilityData;

    protected ServerLevelInject(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l, i);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$addInitCapabilities(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List list, boolean bl2, CallbackInfo ci) {
        this.initCapabilities();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WritableLevelData;getDayTime()J"), method = "tick")
    public long kilt$useLevelDaytime(WritableLevelData instance) {
        return this.getDayTime();
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    public long kilt$useForgeDaytime(long l) {
        return ForgeEventFactory.onSleepFinished((ServerLevel) (Object) this, l, this.getDayTime());
    }

    @WrapWithCondition(method = "method_31420", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/Entity;)V"))
    private boolean kilt$checkIfRemovedOrPart(ServerLevel instance, Consumer consumer, Entity entity) {
        return !entity.isRemoved() && !(entity instanceof PartEntity<?>);
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$checkIfAreaLoaded(Biome instance, LevelReader level, BlockPos pos, Operation<Boolean> original, @Local(ordinal = 0) BlockPos pos2) {
        return this.isAreaLoaded(pos2, 1) && original.call(instance, level, pos);
    }

    @Redirect(method = "advanceWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void kilt$useDimensionDependentBroadcast(PlayerList instance, Packet<?> packet) {
        instance.broadcastAll(packet, this.dimension());
    }

    @WrapWithCondition(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private boolean kilt$checkCanUpdateNonPassenger(Entity instance) {
        return instance.canUpdate();
    }

    @WrapWithCondition(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    private boolean kilt$checkCanUpdatePassenger(Entity instance) {
        return instance.canUpdate();
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void kilt$callSaveEvent(ProgressListener progress, boolean flush, boolean skipSave, CallbackInfo ci) {
        if (!skipSave) {
            MinecraftForge.EVENT_BUS.post(new LevelEvent.Save(this));
        }
    }

    @Inject(method = "addPlayer", at = @At("HEAD"), cancellable = true)
    private void kilt$callJoinLevelEvent(ServerPlayer player, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(player, this)))
            ci.cancel();
    }

    // TODO: onAddedToWorld?

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$modifyPositionedSound(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) double x, @Local(argsOnly = true, ordinal = 1) double y, @Local(argsOnly = true, ordinal = 2) double z, @Local(argsOnly = true) LocalRef<SoundEvent> sound, @Local(argsOnly = true) LocalRef<SoundSource> source, @Local(argsOnly = true, ordinal = 0) LocalFloatRef volume, @Local(argsOnly = true, ordinal = 1) LocalFloatRef pitch, @Local(argsOnly = true) long seed) {
        var event = ForgeEventFactory.onPlaySoundAtPosition(this, x, y, z, sound.get(), source.get(), volume.get(), pitch.get());

        if (event.isCanceled() || event.getSound() == null) {
            ci.cancel();
            return;
        }

        sound.set(event.getSound());
        source.set(event.getSource());
        volume.set(event.getNewVolume());
        pitch.set(event.getNewPitch());
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$modifyEntitySound(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) LocalRef<SoundEvent> sound, @Local(argsOnly = true) LocalRef<SoundSource> source, @Local(argsOnly = true, ordinal = 0) LocalFloatRef volume, @Local(argsOnly = true, ordinal = 1) LocalFloatRef pitch, @Local(argsOnly = true) long seed) {
        var event = ForgeEventFactory.onPlaySoundAtEntity(entity, sound.get(), source.get(), volume.get(), pitch.get());

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
    private void kilt$callGameEvent(GameEvent event, Vec3 position, GameEvent.Context context, CallbackInfo ci) {
        if (!ForgeHooks.onVanillaGameEvent(this, event, position, context))
            ci.cancel();
    }

    @Inject(method = "updateNeighborsAt", at = @At("HEAD"))
    private void kilt$notifyNeighborsEvent(BlockPos pos, Block block, CallbackInfo ci) {
        ForgeEventFactory.onNeighborNotify(this, pos, this.getBlockState(pos), EnumSet.allOf(Direction.class), false).isCanceled(); // why?
    }

    @Inject(method = "updateNeighborsAtExceptFromFacing", at = @At("HEAD"), cancellable = true)
    private void kilt$notifyNeighborsEvent(BlockPos pos, Block blockType, Direction skipSide, CallbackInfo ci) {
        var directions = EnumSet.allOf(Direction.class);
        directions.remove(skipSide);
        if (ForgeEventFactory.onNeighborNotify(this, pos, this.getBlockState(pos), directions, false).isCanceled())
            ci.cancel();
    }

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Explosion;explode()V", shift = At.Shift.BEFORE), cancellable = true)
    private void kilt$callExplosionStart(Entity exploder, DamageSource damageSource, ExplosionDamageCalculator context, double x, double y, double z, float size, boolean causesFire, Explosion.BlockInteraction mode, CallbackInfoReturnable<Explosion> cir, @Local Explosion explosion) {
        if (ForgeEventFactory.onExplosionStart(this, explosion))
            cir.setReturnValue(explosion);
    }

    @Override
    public Int2ObjectMap<PartEntity<?>> kilt$getEntityParts() {
        return this.kilt$entityParts;
    }

    @Override
    public Collection<PartEntity<?>> kilt$getPartEntities() {
        return this.kilt$entityParts.values();
    }

    protected void initCapabilities() {
        this.gatherCapabilities();
        capabilityData = this.getDataStorage().computeIfAbsent(e -> LevelCapabilityData.load(e, this.getCapabilities()), () -> new LevelCapabilityData(getCapabilities()), LevelCapabilityData.ID);
        capabilityData.setCapabilities(getCapabilities());
    }


    @Mixin(targets = "net.minecraft.server.level.ServerLevel.EntityCallbacks")
    public static abstract class EntityCallbacksInject implements LevelCallback<Entity> {
        @Shadow @Final
        ServerLevel field_26936;

        @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
        @Definition(id = "EnderDragon", type = EnderDragon.class)
        @Expression("entity instanceof EnderDragon")
        @WrapOperation(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$startTrackingMultipart(Object object, Operation<Boolean> original) {
            if (original.call(object))
                return true;

            if (((Entity) object).isMultipartEntity()) {
                for (PartEntity<?> part : ((Entity) object).getParts()) {
                    ((ServerLevelInjection) field_26936).kilt$getEntityParts().put(part.getId(), part);
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
                for (PartEntity<?> part : ((Entity) object).getParts()) {
                    ((ServerLevelInjection) field_26936).kilt$getEntityParts().remove(part.getId());
                }
            }

            return false;
        }

        @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
        private void kilt$callEntityLevelRemoveEvent(Entity entity, CallbackInfo ci) {
            entity.onRemovedFromWorld();
            MinecraftForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, field_26936));
        }
    }
}
