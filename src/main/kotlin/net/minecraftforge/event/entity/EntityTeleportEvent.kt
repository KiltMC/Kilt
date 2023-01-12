package net.minecraftforge.event.entity

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrownEnderpearl
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
open class EntityTeleportEvent(entity: Entity, var targetX: Double, var targetY: Double, var targetZ: Double) : EntityEvent(entity) {
    val prevX: Double
        get() = entity!!.x

    val prevY: Double
        get() = entity!!.y

    val prevZ: Double
        get() = entity!!.z

    val prev: Vec3
        get() = entity!!.position()

    val target: Vec3
        get() = Vec3(targetX, targetY, targetZ)

    @Cancelable
    class TeleportCommand(entity: Entity, targetX: Double, targetY: Double, targetZ: Double) : EntityTeleportEvent(entity, targetX, targetY, targetZ)

    @Cancelable
    class SpreadPlayersCommand(entity: Entity, targetX: Double, targetY: Double, targetZ: Double) : EntityTeleportEvent(entity, targetX, targetY, targetZ)

    @Cancelable
    class EnderEntity(entity: LivingEntity, targetX: Double, targetY: Double, targetZ: Double) : EntityTeleportEvent(entity, targetX, targetY, targetZ) {
        val entityLiving: LivingEntity = entity
    }

    @Cancelable
    class EnderPearl(entity: ServerPlayer, targetX: Double, targetY: Double, targetZ: Double, val pearlEntity: ThrownEnderpearl, var attackDamage: Float, val hitResult: HitResult?) : EntityTeleportEvent(entity, targetX, targetY, targetZ) {
        constructor(entity: ServerPlayer, targetX: Double, targetY: Double, targetZ: Double, pearlEntity: ThrownEnderpearl, attackDamage: Float) : this(entity, targetX, targetY, targetZ, pearlEntity, attackDamage, null)
    }

    @Cancelable
    class ChorusFruit(entity: LivingEntity, targetX: Double, targetY: Double, targetZ: Double) : EntityTeleportEvent(entity, targetX, targetY, targetZ) {
        val entityLiving: LivingEntity = entity
    }
}