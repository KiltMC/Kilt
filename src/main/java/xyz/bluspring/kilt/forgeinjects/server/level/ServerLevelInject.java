package xyz.bluspring.kilt.forgeinjects.server.level;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.common.util.LevelCapabilityData;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelInject extends Level {
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

    protected void initCapabilities() {
        this.gatherCapabilities();
        capabilityData = this.getDataStorage().computeIfAbsent(e -> LevelCapabilityData.load(e, this.getCapabilities()), () -> new LevelCapabilityData(getCapabilities()), LevelCapabilityData.ID);
        capabilityData.setCapabilities(getCapabilities());
    }
}
