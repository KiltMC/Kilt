package xyz.bluspring.kilt.forgeinjects.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraftforge.common.util.TriPredicate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.entity.SpawnPlacementsTypeInjection;
import xyz.bluspring.kilt.remaps.world.level.NaturalSpawnerRemap;

@Mixin(SpawnPlacements.Type.class)
public class SpawnPlacementsTypeInject implements SpawnPlacementsTypeInjection {
    private TriPredicate<LevelReader, BlockPos, EntityType<?>> predicate;

    @Shadow @Final public static SpawnPlacements.Type NO_RESTRICTIONS;

    @Override
    public boolean canSpawnAt(LevelReader world, BlockPos pos, EntityType<?> type) {
        if ((Object) this == NO_RESTRICTIONS)
            return true;

        if (predicate == null)
            return NaturalSpawnerRemap.canSpawnAtBody((SpawnPlacements.Type) (Object) this, world, pos, type);

        return predicate.test(world, pos, type);
    }

    @Override
    public void setPredicate(TriPredicate<LevelReader, BlockPos, EntityType<? extends Mob>> predicate) {
        this.predicate = (TriPredicate<LevelReader, BlockPos, EntityType<?>>) (Object) predicate;
    }
}
