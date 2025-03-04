package xyz.bluspring.kilt.forgeinjects.server.level;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.minecraftforge.common.util.LevelCapabilityData;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.level.ServerLevelInjection;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
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
        return ((ServerLevel) (Object) this).getDayTime();
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    public long kilt$useForgeDaytime(long l) {
        return ForgeEventFactory.onSleepFinished((ServerLevel) (Object) this, l, ((ServerLevel) (Object) this).getDayTime());
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
