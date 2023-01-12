package net.minecraftforge.event.level

import net.minecraft.core.BlockPos
import net.minecraft.util.random.WeightedRandomList
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.storage.ServerLevelData
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event

open class LevelEvent(val level: LevelAccessor) : Event() {

    class Load(level: LevelAccessor) : LevelEvent(level)
    class Unload(level: LevelAccessor) : LevelEvent(level)
    class Save(level: LevelAccessor) : LevelEvent(level)

    @Cancelable
    class CreateSpawnPosition(level: LevelAccessor, val settings: ServerLevelData) : LevelEvent(level)

    class PotentialSpawns(level: LevelAccessor, category: MobCategory, val pos: BlockPos, oldList: WeightedRandomList<MobSpawnSettings.SpawnerData>) : LevelEvent(level) {
        // In Forge, there is a second variable named "view", but I guess it somehow
        // auto-updates when list gets updated?
        private val list = mutableListOf<MobSpawnSettings.SpawnerData>().apply {
            if (!oldList.isEmpty)
                addAll(oldList.unwrap())
        }

        val mobCategory = category
        val spawnerDataList: List<MobSpawnSettings.SpawnerData>
            get() = list

        fun addSpawnerData(data: MobSpawnSettings.SpawnerData) {
            list.add(data)
        }

        fun removeSpawnerData(data: MobSpawnSettings.SpawnerData): Boolean {
            return list.remove(data)
        }
    }
}