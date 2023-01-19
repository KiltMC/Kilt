package xyz.bluspring.kilt.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SpawnPlacements.class)
public interface SpawnPlacementsAccessor {
    @Accessor("DATA_BY_TYPE")
    static Map<EntityType<?>, SpawnPlacements.Data> getDataByType() {
        throw new UnsupportedOperationException();
    }
}
