// TRACKED HASH: 463588f2acf28725b91608efe6c2270022c2dde9
package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.NaturalSpawnerInjection;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerInject implements NaturalSpawnerInjection {

    @WrapOperation(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getMaxSpawnClusterSize()I")
    )
    private static int kilt$getMaxPackSizeEvent(Mob instance, Operation<Integer> original) {

        return ForgeEventFactory.kilt$getMaxSpawnPackSize(instance, original);
    }

    @CreateStatic
    private static boolean canSpawnAtBody(SpawnPlacements.Type type, LevelReader levelReader, BlockPos blockPos, EntityType<?> entityType) {
        return NaturalSpawnerInjection.canSpawnAtBody(type, levelReader, blockPos, entityType);
    }
}