package xyz.bluspring.kilt.mixin.workarounds.finalize_spawn;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.workarounds.ForgeEventFactoryWorkaround;

@Mixin(value = ForgeEventFactory.class, priority = 950)
public class ForgeEventFactoryMixin {

    @WrapOperation(
            method = "onFinalizeSpawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;"
            )
    )
    private static SpawnGroupData kilt$onFinalizeSpawn(Mob instance, ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, SpawnGroupData spawnGroupData, CompoundTag compoundTag, Operation<SpawnGroupData> forgeOriginal) {
        try {
            ForgeEventFactoryWorkaround.kilt$hasFiredInitializeEvent.get().add(instance);
            var actualOriginal = ForgeEventFactoryWorkaround.kilt$fabricOriginal.get();
            ForgeEventFactoryWorkaround.kilt$fabricOriginal.set(null);
            if (actualOriginal == null) {
                actualOriginal = forgeOriginal;
            }
            return actualOriginal.call(instance, serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        } finally {
            ForgeEventFactoryWorkaround.kilt$hasFiredInitializeEvent.get().remove(instance);
        }
    }

}
