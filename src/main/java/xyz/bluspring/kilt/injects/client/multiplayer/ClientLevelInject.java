// TRACKED HASH: 9493f81a6485a3765611155c032cf421d0ceeaf2
package xyz.bluspring.kilt.injects.client.multiplayer;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.neoforged.neoforge.client.ColorResolverManager;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IForgeLevel;
import net.minecraftforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.multiplayer.ClientLevelInjection;

import java.util.Collection;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelInject extends Level implements ClientLevelInjection, IForgeLevel {
    @Unique private final ModelDataManager modelDataManager = new ModelDataManager((ClientLevel) (Object) this);
    @Unique private final Int2ObjectMap<PartEntity<?>> kilt$partEntities = new Int2ObjectOpenHashMap<>();

    protected ClientLevelInject(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow public abstract DimensionSpecialEffects effects();

    @Inject(at = @At("TAIL"), method = "method_23778")
    public void kilt$registerForgeBlockTintCaches(Object2ObjectArrayMap<ColorResolver, BlockTintCache> object2ObjectArrayMap, CallbackInfo ci) {
        ColorResolverManager.registerBlockTintCaches((ClientLevel) (Object) this, object2ObjectArrayMap);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$initLevel(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey resourceKey, Holder holder, int i, int j, Supplier supplier, LevelRenderer levelRenderer, boolean bl, long l, CallbackInfo ci) {
        this.gatherCapabilities();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load((ClientLevel) (Object) this));
    }

    @WrapWithCondition(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    public boolean kilt$onlyTickIfCanUpdate(Entity entity) {
        return entity.canUpdate();
    }

    @Inject(at = @At("HEAD"), method = "addEntity", cancellable = true)
    public void kilt$runJoinLevelEvent(int i, Entity entity, CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, (ClientLevel) (Object) this)))
            ci.cancel();
    }

    @Inject(at = @At("TAIL"), method = "addEntity")
    public void kilt$addEntityToWorld(int i, Entity entity, CallbackInfo ci) {
        entity.onAddedToWorld();
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$callPlayLevelSoundAtPositionEvent(CallbackInfo ci, @Local(argsOnly = true) LocalRef<Holder<SoundEvent>> sound, @Local(argsOnly = true) LocalRef<SoundSource> source, @Local(argsOnly = true, ordinal = 0) LocalFloatRef volume, @Local(argsOnly = true, ordinal = 1) LocalFloatRef pitch, @Local(argsOnly = true) long seed, @Local(argsOnly = true, ordinal = 0) double x, @Local(argsOnly = true, ordinal = 1) double y, @Local(argsOnly = true, ordinal = 2) double z) {
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

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), cancellable = true)
    private void kilt$callPlayLevelSoundAtEntityEvent(CallbackInfo ci, @Local(argsOnly = true) LocalRef<Holder<SoundEvent>> sound, @Local(argsOnly = true) LocalRef<SoundSource> source, @Local(argsOnly = true, ordinal = 0) LocalFloatRef volume, @Local(argsOnly = true, ordinal = 1) LocalFloatRef pitch, @Local(argsOnly = true) long seed, @Local(argsOnly = true) Entity entity) {
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

    @Mixin(ClientLevel.ClientLevelData.class)
    public abstract static class ClientLevelDataInject {
        @Shadow private Difficulty difficulty;

        @Inject(method = "setDifficulty", at = @At("HEAD"))
        private void kilt$callForgeDifficultyChangeEvent(Difficulty difficulty, CallbackInfo ci) {
            CommonHooks.onDifficultyChange(difficulty, this.difficulty);
        }
    }

    @Mixin(targets = "net/minecraft/client/multiplayer/ClientLevel$EntityCallbacks")
    public abstract static class EntityCallbacksInject {
        @Shadow @Final private ClientLevel field_27735;

        @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
        private void kilt$addForgeMultipartEntitiesToLevel(Entity entity, CallbackInfo ci) {
            if (entity.isMultipartEntity()) {
                for (PartEntity<?> part : entity.getParts()) {
                    ((ClientLevelInjection) field_27735).kilt$getPartEntitiesMap().put(part.getId(), part);
                }
            }
        }

        @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
        private void kilt$removeForgeMultipartEntitiesFromLevel(Entity entity, CallbackInfo ci) {
            entity.onRemovedFromWorld();
            NeoForge.EVENT_BUS.post(new EntityLeaveLevelEvent(entity, field_27735));

            if (entity.isMultipartEntity()) {
                for (PartEntity<?> part : entity.getParts()) {
                    ((ClientLevelInjection) field_27735).kilt$getPartEntitiesMap().remove(part.getId());
                }
            }
        }
    }

    @Override
    public Collection<PartEntity<?>> kilt$getPartEntities() {
        return this.kilt$partEntities.values();
    }

    @Override
    public ModelDataManager getModelDataManager() {
        return modelDataManager;
    }

    @Unique
    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        var constantAmbientLight = this.effects().constantAmbientLight();

        if (!shade)
            return constantAmbientLight ? 0.9F : 1F;

        return QuadLighter.calculateShade(normalX, normalY, normalZ, constantAmbientLight);
    }

    @Override
    public Int2ObjectMap<PartEntity<?>> kilt$getPartEntitiesMap() {
        return this.kilt$partEntities;
    }
}