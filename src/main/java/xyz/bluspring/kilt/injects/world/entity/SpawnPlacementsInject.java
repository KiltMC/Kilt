// TRACKED HASH: 604c32f8a695e1a5a43593eec26599fd0fc2203c
package xyz.bluspring.kilt.injects.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.entity.SpawnPlacementsInjection;
import xyz.bluspring.kilt.injections.entity.SpawnPlacementsTypeInjection;
import xyz.bluspring.kilt.remaps.world.level.NaturalSpawnerRemap;

@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsInject implements SpawnPlacementsInjection {
    @ModifyReturnValue(method = "checkSpawnRules", at = @At("RETURN"))
    private static <T extends Entity> boolean kilt$checkForgeSpawnPlacements(boolean original, EntityType<T> entityType, ServerLevelAccessor serverLevel, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return EventHooks.checkSpawnPlacements(entityType, serverLevel, spawnType, pos, random, original);
    }

    @Mixin(SpawnPlacements.Type.class)
    public static class TypeInject implements SpawnPlacementsTypeInjection {
        @CreateStatic
        private static SpawnPlacements.Type create(String name, TriPredicate<LevelReader, BlockPos, EntityType<? extends Mob>> predicate) {
            return SpawnPlacementsTypeInjection.create(name, predicate);
        }

        private TriPredicate<LevelReader, BlockPos, EntityType<?>> predicate;

        @Shadow
        @Final
        public static SpawnPlacements.Type NO_RESTRICTIONS;

        @Override
        public void kilt$setPredicate(TriPredicate<LevelReader, BlockPos, EntityType<? extends Mob>> predicate) {
            this.predicate = (TriPredicate<LevelReader, BlockPos, EntityType<?>>) (Object) predicate;
        }

        @Override
        public boolean canSpawnAt(LevelReader world, BlockPos pos, EntityType<?> type) {
            if ((Object) this == NO_RESTRICTIONS)
                return true;

            if (predicate == null)
                return NaturalSpawnerRemap.canSpawnAtBody((SpawnPlacements.Type) (Object) this, world, pos, type);

            return predicate.test(world, pos, type);
        }
    }

    @CreateStatic
    private static void fireSpawnPlacementEvent() {
        SpawnPlacementsInjection.fireSpawnPlacementEvent();
    }
}