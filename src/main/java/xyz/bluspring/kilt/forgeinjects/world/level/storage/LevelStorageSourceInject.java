package xyz.bluspring.kilt.forgeinjects.world.level.storage;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.injections.world.level.storage.LevelStorageAccessInjection;
import xyz.bluspring.kilt.mixin.LevelStorageSourceAccessor;

import java.nio.file.Path;

@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceInject {
    @Inject(method = "readWorldGenSettings", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/DataFixer;update(Lcom/mojang/datafixers/DSL$TypeReference;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;", ordinal = 0, shift = At.Shift.AFTER))
    private static <T> void kilt$retainDimensionsFix(Dynamic<T> nbt, DataFixer fixer, int version, CallbackInfoReturnable<Pair<WorldGenSettings, Lifecycle>> cir) {
        if (nbt.getOps() instanceof RegistryOps<T> ops) {
            ops.registryAccess.ownedRegistries().forEach(e -> e.value().freeze());
        }
    }

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
                    ForgeHooks.readAdditionalLevelSaveData(tag, this.levelDirectory);
                } catch (Exception e) {
                    Kilt.Companion.getLogger().error("Exception reading {}", path, e);
                }

                return "";
            });
        }

        @Inject(method = "saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;", ordinal = 0))
        private void kilt$writeModdedSaveData(RegistryAccess registries, WorldData serverConfiguration, CompoundTag hostPlayerNBT, CallbackInfo ci, @Local(ordinal = 2) CompoundTag tag) {
            ForgeHooks.writeAdditionalLevelSaveData(serverConfiguration, tag);
        }

        public Path getWorldDir() {
            return ((LevelStorageSourceAccessor) field_23766).getBaseDir();
        }
    }
}