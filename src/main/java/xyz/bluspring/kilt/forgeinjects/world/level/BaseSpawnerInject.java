package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IOwnedSpawner;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerInject implements IOwnedSpawner {
    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isEmpty()Z", ordinal = 1))
    private boolean kilt$tryCheckSpawnPosition(Optional<SpawnData.CustomSpawnRules> instance, Operation<Boolean> original, @Local Mob mob, @Local(argsOnly = true) ServerLevel level, @Local SpawnData spawnData) {
        // TODO: find some way to improve this check
        if (!EventHooks.checkSpawnPositionSpawner(mob, level, MobSpawnType.SPAWNER, spawnData, (BaseSpawner) (Object) this)) {
            return true;
        }

        return original.call(instance);
    }

    @Definition(id = "bl2", local = @Local(type = boolean.class, ordinal = 1))
    @Expression("bl2")
    @ModifyExpressionValue(method = "serverTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryFinalizeSpawn(boolean original, @Local Mob mob, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
        var event = EventHooks.finalizeMobSpawnSpawner(mob, level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null, this, false);
        return !event.isCanceled() && original;
    }

    @Override
    public @Nullable Either<BlockEntity, Entity> getOwner() {
        return null;
    }
}
