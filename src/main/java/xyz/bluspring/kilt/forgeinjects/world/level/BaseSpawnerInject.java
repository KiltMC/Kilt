package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.BaseSpawnerInjection;

import java.util.Optional;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerInject implements BaseSpawnerInjection {
    @Override
    public Entity getSpawnerEntity() {
        return null;
    }

    @Override
    public BlockEntity getSpawnerBlockEntity() {
        return null;
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isEmpty()Z", ordinal = 1))
    private boolean kilt$tryCheckSpawnPosition(Optional<SpawnData.CustomSpawnRules> instance, Operation<Boolean> original, @Local Mob mob, @Local(argsOnly = true) ServerLevel level, @Local SpawnData spawnData) {
        // TODO: find some way to improve this check
        if (!ForgeEventFactory.checkSpawnPositionSpawner(mob, level, MobSpawnType.SPAWNER, spawnData, (BaseSpawner) (Object) this)) {
            return true;
        }

        return original.call(instance);
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private SpawnGroupData kilt$tryFinalizeSpawn(Mob instance, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag dataTag, Operation<SpawnGroupData> original) {
        var event = ForgeEventFactory.onFinalizeSpawnSpawner(instance, level, difficulty, null, dataTag, (BaseSpawner) (Object) this);

        if (event == null)
            return null;

        return original.call(instance, level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData(), event.getSpawnTag());
    }
}
