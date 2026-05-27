package xyz.bluspring.kilt.loader.asm.template;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo // Pseudo is needed to avoid compile error, we remove it when generating the actual mixin.
@Mixin(targets = {""}, priority = 1050) // We populate targets at runtime.
public class FinalizeSpawnTemplate {

    @WrapMethod(method = "finalizeSpawn")
    public SpawnGroupData kilt$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag dataTag, Operation<SpawnGroupData> original) {
        if (ForgeEventFactory.kilt$hasFiredInitializeEvent.get().contains(this)) {
            return original.call(level, difficulty, reason, spawnData, dataTag);
        }
        return ForgeEventFactory.kilt$onFinalizeSpawn(
                (Mob) (Object) this, level, difficulty, reason, spawnData, dataTag,
                args -> {
                    var newLevel = (ServerLevelAccessor) args[1];
                    var newDifficulty = (DifficultyInstance) args[2];
                    var newReason = (MobSpawnType) args[3];
                    var newSpawnData = (SpawnGroupData) args[4];
                    var newTagData = (CompoundTag) args[5];
                    if (args[0] == this) {
                        return original.call(newLevel, newDifficulty, newReason, newSpawnData, newTagData);
                    } else {
                        return ((Mob) args[0]).finalizeSpawn(newLevel, newDifficulty, newReason, newSpawnData, newTagData);
                    }
                }
        );
    }

}
