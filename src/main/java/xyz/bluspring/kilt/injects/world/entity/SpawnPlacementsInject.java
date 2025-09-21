// TRACKED HASH: 604c32f8a695e1a5a43593eec26599fd0fc2203c
package xyz.bluspring.kilt.injects.world.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.entity.SpawnPlacementsInjection;

import java.util.Map;

@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsInject implements SpawnPlacementsInjection {
    @Shadow @Final public static Map<EntityType<?>, SpawnPlacements.Data> DATA_BY_TYPE;

    @ModifyReturnValue(method = "checkSpawnRules", at = @At("RETURN"))
    private static <T extends Entity> boolean kilt$checkForgeSpawnPlacements(boolean original, EntityType<T> entityType, ServerLevelAccessor serverLevel, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return EventHooks.checkSpawnPlacements(entityType, serverLevel, spawnType, pos, random, original);
    }

    @CreateStatic
    private static boolean hasPlacement(EntityType<?> type) {
        return SpawnPlacementsInjection.hasPlacement(type);
    }

    @CreateStatic
    private static void fireSpawnPlacementEvent() {
        SpawnPlacementsInjection.fireSpawnPlacementEvent();
    }
}