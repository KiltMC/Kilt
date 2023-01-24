package xyz.bluspring.kilt.remaps.world.entity.vehicle

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.vehicle.AbstractMinecart
import net.minecraft.world.level.Level
import net.minecraftforge.common.IMinecartCollisionHandler

abstract class AbstractMinecartRemap : AbstractMinecart {
    constructor(entityType: EntityType<*>, level: Level) : super(entityType, level)
    constructor(entityType: EntityType<*>, level: Level, i: Double, j: Double, k: Double) : super(entityType, level, i, j, k)

    companion object {
        @JvmStatic
        fun registerCollisionHandler(handler: IMinecartCollisionHandler) {
            // TODO: Add injects to register collision handlers
        }
    }
}