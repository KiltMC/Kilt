package xyz.bluspring.kilt.remaps.world.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.SpawnPlacements
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent
import net.minecraftforge.fml.ModLoader
import xyz.bluspring.kilt.mixin.SpawnPlacementsAccessor

object SpawnPlacementsRemap : SpawnPlacements() {
    @JvmStatic
    fun fireSpawnPlacementEvent() {
        val map = mutableMapOf<EntityType<*>, SpawnPlacementRegisterEvent.MergedSpawnPredicate<*>>()

        DATA_BY_TYPE.forEach { (entityType, data) ->
            map[entityType] = SpawnPlacementRegisterEvent.MergedSpawnPredicate(data.predicate, data.placement, data.heightMap)
        }
        ModLoader.get().postEvent(SpawnPlacementRegisterEvent(map))

        map.forEach { (entityType, merged) ->
            DATA_BY_TYPE[entityType] = SpawnPlacements.Data(merged.heightmapType, merged.spawnType, merged.build())
        }
    }
}