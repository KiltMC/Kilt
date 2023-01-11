package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.LevelAccessor
import net.minecraftforge.eventbus.api.Cancelable

open class LivingSpawnEvent(mob: Mob, val level: LevelAccessor, val x: Double, val y: Double, val z: Double) : LivingEvent(mob) {
    override val entity: Mob = mob

    @HasResult
    class CheckSpawn(mob: Mob, level: LevelAccessor, x: Double, y: Double, z: Double, val spawner: BaseSpawner?, val spawnReason: MobSpawnType) : LivingSpawnEvent(mob, level, x, y, z)

    @Cancelable
    class SpecialSpawn(mob: Mob, level: LevelAccessor, x: Double, y: Double, z: Double, val spawner: BaseSpawner?, val spawnReason: MobSpawnType) : LivingSpawnEvent(mob, level, x, y, z)

    @HasResult
    class AllowDespawn(mob: Mob) : LivingSpawnEvent(mob, mob.level, mob.x, mob.y, mob.z)
}