package xyz.bluspring.kilt.injects.world.level.storage;

import java.nio.file.Path;
import java.util.UUID;

import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.injections.world.level.storage.LevelStorageAccessInjection;
import xyz.bluspring.kilt.mixin.LevelStorageSourceAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceInject {
    @Mixin(LevelStorageSource.LevelStorageAccess.class)
    public abstract static class LevelStorageAccessInject implements LevelStorageAccessInjection {
        @Shadow protected abstract void checkLock();
        @Shadow @Final private LevelStorageSource.LevelDirectory levelDirectory;

        @Shadow
        @Final
        private LevelStorageSource this$0;

        @Override
        public void readAdditionalLevelSaveData(boolean fallback) {
            this.checkLock();
            Path path = fallback ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile();
            try {
                var tag = LevelStorageSourceAccessor.callReadLightweightData(path);
                if (tag instanceof CompoundTag compoundTag) {
                    CommonHooks.readAdditionalLevelSaveData(compoundTag, this.levelDirectory);
                }
            } catch (Exception e) {
                Kilt.Companion.getLogger().error("Exception reading {}", path, e);
            }
        }

        @Inject(method = "saveDataTag(Lnet/minecraft/world/level/storage/WorldData;Ljava/util/UUID;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;saveLevelData(Lnet/minecraft/nbt/CompoundTag;)V"))
        private void kilt$writeAdditionalLevelSaveData(WorldData levelData, UUID singleplayerUUID, CallbackInfo ci, @Local(name = "root") CompoundTag root) {
            CommonHooks.writeAdditionalLevelSaveData(levelData, root);
        }

        @Override
        public Path getWorldDir() {
            return this$0.getBaseDir();
        }
    }
}
