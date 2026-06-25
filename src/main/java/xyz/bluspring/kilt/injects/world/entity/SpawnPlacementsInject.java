// TRACKED HASH: 604c32f8a695e1a5a43593eec26599fd0fc2203c
package xyz.bluspring.kilt.injects.world.entity;

import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.entity.SpawnPlacementsInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsInject implements SpawnPlacementsInjection {
    @Shadow @Final public static Map<EntityType<?>, SpawnPlacements.Data> DATA_BY_TYPE;

    @ModifyReturnValue(method = "checkSpawnRules", at = @At("RETURN"))
    private static <T extends Entity> boolean kilt$checkForgeSpawnPlacements(boolean original, final EntityType<T> type, final ServerLevelAccessor level, final EntitySpawnReason spawnReason, final BlockPos pos, final RandomSource random) {
        return EventHooks.checkSpawnPlacements(type, level, spawnReason, pos, random, original);
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
