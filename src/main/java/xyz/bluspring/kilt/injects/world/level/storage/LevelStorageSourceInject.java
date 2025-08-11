// TRACKED HASH: 08be86b407d334fa12a96774c87371939bddeb23
package xyz.bluspring.kilt.injects.world.level.storage;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
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

import java.nio.file.Path;

@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceInject {
    @Mixin(LevelStorageSource.LevelStorageAccess.class)
    public abstract static class LevelStorageAccessInject implements LevelStorageAccessInjection {
        @Shadow protected abstract void checkLock();

        @Shadow @Final private LevelStorageSource field_23766;

        @Shadow @Final private LevelStorageSource.LevelDirectory levelDirectory;

        public void readAdditionalLevelSaveData() {
            checkLock();
            ((LevelStorageSourceAccessor) field_23766).callReadLevelData(this.levelDirectory, (path, dataFixer) -> {
                try {
                    CompoundTag tag = NbtIo.readCompressed(path.toFile());
                    CommonHooks.readAdditionalLevelSaveData(tag, this.levelDirectory);
                } catch (Exception e) {
                    Kilt.Companion.getLogger().error("Exception reading {}", path, e);
                }

                return "";
            });
        }

        @Inject(method = "saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", ordinal = 0))
        private void kilt$writeModdedSaveData(RegistryAccess registries, WorldData serverConfiguration, CompoundTag hostPlayerNBT, CallbackInfo ci, @Local(ordinal = 2) CompoundTag tag) {
            CommonHooks.writeAdditionalLevelSaveData(serverConfiguration, tag);
        }

        public Path getWorldDir() {
            return ((LevelStorageSourceAccessor) field_23766).getBaseDir();
        }
    }
}