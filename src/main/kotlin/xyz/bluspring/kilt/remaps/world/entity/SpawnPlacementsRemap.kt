package xyz.bluspring.kilt.remaps.world.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.SpawnPlacements
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent
import xyz.bluspring.kilt.mixin.SpawnPlacementsAccessor

object SpawnPlacementsRemap {
    @JvmStatic
    fun fireSpawnPlacementEvent() {
        val map = mutableMapOf<EntityType<*>, SpawnPlacementRegisterEvent.MergedSpawnPredicate<*>>()

        SpawnPlacementsAccessor.getDataByType().forEach { (entityType, data) ->
            map[entityType] = SpawnPlacementRegisterEvent.MergedSpawnPredicate(data)
        }
    }
}