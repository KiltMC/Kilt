package xyz.bluspring.kilt.injects.world.level.block.entity.trialspawner;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.neoforged.neoforge.common.extensions.IOwnedSpawner;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;

@Mixin(TrialSpawner.class)
public abstract class TrialSpawnerInject implements IOwnedSpawner {
    @Shadow @Final private TrialSpawner.StateAccessor stateAccessor;

    @Definition(id = "bl", local = @Local(type = boolean.class))
    @Expression("bl")
    @ModifyExpressionValue(method = "spawnMob", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$alwaysReturnTrue(boolean original) {
        return true;
    }

    @WrapOperation(method = "spawnMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData kilt$handleMobSpawnEvent(Mob instance, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, Operation<SpawnGroupData> original, @Local boolean flag) {
        return EventHooks.kilt$finalizeMobSpawnSpawner(instance, level, difficulty, spawnType, spawnGroupData, this, flag, original);
    }

    @Override
    public @Nullable Either<BlockEntity, Entity> getOwner() {
        if (this.stateAccessor instanceof TrialSpawnerBlockEntity blockEntity) {
            return Either.left(blockEntity);
        }

        return null;
    }
}
