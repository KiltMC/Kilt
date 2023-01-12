package net.minecraftforge.event.level

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.Level
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event

open class ExplosionEvent(val level: Level, val explosion: Explosion) : Event() {
    @Cancelable
    class Start(level: Level, explosion: Explosion) : ExplosionEvent(level, explosion)

    class Detonate(level: Level, explosion: Explosion, entityList: List<Entity>) : ExplosionEvent(level, explosion) {
        val affectedEntities = entityList
        val affectedBlocks: List<BlockPos> = explosion.toBlow
    }
}