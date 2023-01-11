package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.level.Level
import net.minecraftforge.event.entity.EntityEvent

open class ZombieEvent(zombie: Zombie) : EntityEvent(zombie) {
    override val entity: Zombie = zombie

    @HasResult
    class SummonAidEvent(zombie: Zombie, val level: Level, val x: Int, val y: Int, val z: Int, val attacker: LivingEntity, val summonChance: Double) : ZombieEvent(zombie) {
        var customSummonedAid: Zombie? = null
    }
}