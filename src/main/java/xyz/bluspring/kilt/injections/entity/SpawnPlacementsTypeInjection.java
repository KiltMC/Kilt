package xyz.bluspring.kilt.injections.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.util.TriPredicate;
import xyz.bluspring.kilt.mixin.TypeAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface SpawnPlacementsTypeInjection {
    static SpawnPlacements.Type create(String name, TriPredicate<LevelReader, BlockPos, EntityType<? extends Mob>> predicate) {
        var value = EnumUtils.addEnumToClass(
                SpawnPlacements.Type.class,
                TypeAccessor.getValues(),
                name, (size) -> TypeAccessor.createType(name, size),
                (values) -> TypeAccessor.setValues(values.toArray(new SpawnPlacements.Type[0]))
        );

        ((SpawnPlacementsTypeInjection) (Object) value).setPredicate(predicate);
        return value;
    }

    default boolean canSpawnAt(LevelReader world, BlockPos pos, EntityType<?> type) {
        throw new IllegalStateException();
    }

    default void setPredicate(TriPredicate<LevelReader, BlockPos, EntityType<? extends Mob>> predicate) {
        throw new IllegalStateException();
    }
}
