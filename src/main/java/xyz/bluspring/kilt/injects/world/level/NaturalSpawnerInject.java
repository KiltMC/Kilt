// TRACKED HASH: 463588f2acf28725b91608efe6c2270022c2dde9
package xyz.bluspring.kilt.injects.world.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.NaturalSpawnerInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerInject implements NaturalSpawnerInjection {
    @WrapOperation(method = "createState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;getCategory()Lnet/minecraft/world/entity/MobCategory;"))
    private static MobCategory kilt$tryUseNeoClassification(EntityType<?> instance, Operation<MobCategory> original, @Local Entity entity) {
        if (KiltHelper.INSTANCE.hasMethodOverride(entity.getClass(), IEntityExtension.class, "getClassification", boolean.class)) {
            return entity.getClassification(true);
        }

        return original.call(instance);
    }

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getMaxSpawnClusterSize()I")
    )
    private static int kilt$getMaxPackSizeEvent(Mob instance, Operation<Integer> original) {
        return EventHooks.kilt$getMaxSpawnClusterSize(instance, original);
    }

    @WrapOperation(method = "isValidPositionForMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"))
    private static boolean kilt$useNeoCheckSpawnPosition(Mob instance, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, Operation<Boolean> original, @Share("result") LocalRef<MobSpawnEvent.PositionCheck.Result> result) {
        result.set(EventHooks.kilt$checkSpawnPosition(instance, (ServerLevelAccessor) levelAccessor, mobSpawnType));

        if (result.get() != MobSpawnEvent.PositionCheck.Result.DEFAULT) {
            return result.get() == MobSpawnEvent.PositionCheck.Result.SUCCEED;
        }

        return original.call(instance, levelAccessor, mobSpawnType);
    }

    @WrapOperation(method = "isValidPositionForMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnObstruction(Lnet/minecraft/world/level/LevelReader;)Z"))
    private static boolean kilt$useCheckSpawnPosResult(Mob instance, LevelReader levelReader, Operation<Boolean> original, @Share("result") LocalRef<MobSpawnEvent.PositionCheck.Result> result) {
        if (result.get() != MobSpawnEvent.PositionCheck.Result.DEFAULT)
            return result.get() == MobSpawnEvent.PositionCheck.Result.SUCCEED;

        return original.call(instance, levelReader);
    }

    @ModifyExpressionValue(method = "mobsAt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/structures/NetherFortressStructure;FORTRESS_ENEMIES:Lnet/minecraft/util/random/WeightedRandomList;"))
    private static WeightedRandomList<MobSpawnSettings.SpawnerData> kilt$tryUseNeoMonsterSpawns(WeightedRandomList<MobSpawnSettings.SpawnerData> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) MobCategory category, @Local(argsOnly = true) BlockPos pos) {
        var monsterSpawns = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
            .getOrThrow(BuiltinStructures.FORTRESS)
            .spawnOverrides()
            .get(MobCategory.MONSTER);

        // TODO: this might be mod-incompatible...
        if (monsterSpawns != null) {
            return EventHooks.getPotentialSpawns(level, category, pos, monsterSpawns.spawns());
        }

        return EventHooks.getPotentialSpawns(level, category, pos, original);
    }

    @ModifyExpressionValue(method = "mobsAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getMobsAt(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/util/random/WeightedRandomList;"))
    private static WeightedRandomList<MobSpawnSettings.SpawnerData> kilt$checkNeoPotentialSpawns(WeightedRandomList<MobSpawnSettings.SpawnerData> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) MobCategory category, @Local(argsOnly = true) BlockPos pos) {
        return EventHooks.getPotentialSpawns(level, category, pos, original);
    }

    @WrapOperation(method = "spawnMobsForChunkGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"))
    private static boolean kilt$useNeoCheckSpawnPositionChunkGen(Mob instance, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, Operation<Boolean> original, @Share("result") LocalRef<MobSpawnEvent.PositionCheck.Result> result) {
        result.set(EventHooks.kilt$checkSpawnPosition(instance, (ServerLevelAccessor) levelAccessor, mobSpawnType));

        if (result.get() != MobSpawnEvent.PositionCheck.Result.DEFAULT) {
            var value = result.get() == MobSpawnEvent.PositionCheck.Result.SUCCEED;
            if (value)
                result.set(MobSpawnEvent.PositionCheck.Result.DEFAULT);

            return value;
        }

        result.set(MobSpawnEvent.PositionCheck.Result.DEFAULT);
        return original.call(instance, levelAccessor, mobSpawnType);
    }

    @WrapOperation(method = "spawnMobsForChunkGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnObstruction(Lnet/minecraft/world/level/LevelReader;)Z"))
    private static boolean kilt$useCheckSpawnPosResultChunkGen(Mob instance, LevelReader levelReader, Operation<Boolean> original, @Share("result") LocalRef<MobSpawnEvent.PositionCheck.Result> result) {
        if (result.get() != MobSpawnEvent.PositionCheck.Result.DEFAULT) {
            var value = result.get() == MobSpawnEvent.PositionCheck.Result.SUCCEED;
            if (value)
                result.set(MobSpawnEvent.PositionCheck.Result.DEFAULT);

            return value;
        }

        result.set(MobSpawnEvent.PositionCheck.Result.DEFAULT);
        return original.call(instance, levelReader);
    }
}