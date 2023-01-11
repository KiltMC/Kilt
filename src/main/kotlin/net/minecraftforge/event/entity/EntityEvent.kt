package net.minecraftforge.event.entity

import net.minecraft.core.SectionPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.Pose
import net.minecraftforge.eventbus.api.Event
import xyz.bluspring.kilt.mixin.EntityAccessor

open class EntityEvent(open val entity: Entity) : Event() {
    open class EntityConstructing(entity: Entity) : EntityEvent(entity)

    open class EnteringSection(entity: Entity, val packedOldPos: Long, val packedNewPos: Long) : EntityEvent(entity) {
        val oldPos = SectionPos.of(packedOldPos)

        val newPos = SectionPos.of(packedNewPos)

        fun didChunkChange(): Boolean {
            return SectionPos.x(packedOldPos) != SectionPos.x(packedNewPos) || SectionPos.z(packedOldPos) != SectionPos.z(packedNewPos)
        }
    }

    open class Size(
        entity: Entity,
        val pose: Pose,
        val oldSize: EntityDimensions,
        newSize: EntityDimensions,
        val oldEyeHeight: Float,
        var newEyeHeight: Float
    ) : EntityEvent(entity) {
        constructor(entity: Entity, pose: Pose, size: EntityDimensions, defaultEyeHeight: Float) : this(entity, pose, size, size, defaultEyeHeight, defaultEyeHeight)

        private var internalNewSize = newSize

        var newSize: EntityDimensions
            get() = internalNewSize
            set(value) = setNewSize(value, false)

        fun setNewSize(size: EntityDimensions, updateEyeHeight: Boolean) {
            internalNewSize = size
            if (updateEyeHeight) {
                newEyeHeight = (entity as EntityAccessor).callGetEyeHeight(pose, newSize)
            }
        }
    }
}