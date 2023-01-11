package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraftforge.eventbus.api.Cancelable

open class LivingEntityUseItemEvent(entity: LivingEntity, val item: ItemStack, var duration: Int) : LivingEvent(entity) {
    @Cancelable
    class Start(entity: LivingEntity, item: ItemStack, duration: Int) : LivingEntityUseItemEvent(entity, item, duration)

    @Cancelable
    class Tick(entity: LivingEntity, item: ItemStack, duration: Int) : LivingEntityUseItemEvent(entity, item, duration)

    @Cancelable
    class Stop(entity: LivingEntity, item: ItemStack, duration: Int) : LivingEntityUseItemEvent(entity, item, duration)

    @Cancelable
    class Finish(entity: LivingEntity, item: ItemStack, duration: Int, result: ItemStack) : LivingEntityUseItemEvent(entity, item, duration) {
        var resultStack = result
    }
}