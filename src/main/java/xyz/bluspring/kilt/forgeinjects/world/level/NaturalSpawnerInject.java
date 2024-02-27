// TRACKED HASH: 463588f2acf28725b91608efe6c2270022c2dde9
package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.NaturalSpawnerInjection;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerInject implements NaturalSpawnerInjection {
    @CreateStatic
    private static boolean canSpawnAtBody(SpawnPlacements.Type type, LevelReader levelReader, BlockPos blockPos, EntityType<?> entityType) {
        return NaturalSpawnerInjection.canSpawnAtBody(type, levelReader, blockPos, entityType);
    }
}