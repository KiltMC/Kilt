package xyz.bluspring.kilt.mixin;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpawnPlacements.Data.class)
public interface SpawnPlacementsDataAccessor {
    @Accessor
    Heightmap.Types getHeightMap();

    @Accessor
    SpawnPlacements.Type getPlacement();

    @Accessor
    SpawnPlacements.SpawnPredicate<?> getPredicate();
}
