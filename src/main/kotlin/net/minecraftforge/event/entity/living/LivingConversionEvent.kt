package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.eventbus.api.Cancelable
import java.util.function.Consumer

open class LivingConversionEvent(entity: LivingEntity) : LivingEvent(entity) {
    @Cancelable
    class Pre(entity: LivingEntity, val outcome: EntityType<out LivingEntity>, private val timer: Consumer<Int>) : LivingConversionEvent(entity) {
        fun setConversionTimer(ticks: Int) {
            timer.accept(ticks)
        }
    }

    class Post(entity: LivingEntity, val outcome: LivingEntity) : LivingConversionEvent(entity)
}