package xyz.bluspring.kilt.workarounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public interface ForgeEventFactoryWorkaround {

    ThreadLocal<Operation<SpawnGroupData>> kilt$fabricOriginal = ThreadLocal.withInitial(() -> null);

    ThreadLocal<Set<Mob>> kilt$hasFiredInitializeEvent = ThreadLocal.withInitial(HashSet::new);

    // Ideally, I would have wanted to turn onFinalizeSpawn into a stub calling this.
    // Unfortunately: https://github.com/The-Aether-Team/The-Aether/blob/1.20.1-develop/src/main/java/com/aetherteam/aether/mixin/mixins/common/ForgeEventFactoryMixin.java#L24-L29
    static SpawnGroupData kilt$onFinalizeSpawn(
            Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag spawnTag, Operation<SpawnGroupData> original
    ) {
        try {
            kilt$fabricOriginal.set(original);
            return ForgeEventFactory.onFinalizeSpawn(mob, level, difficulty, spawnType, spawnData, spawnTag);
        } finally {
            kilt$fabricOriginal.set(null); // Removing would likely just cause unnecessary lag.
        }
    }

}
