package xyz.bluspring.kilt.remaps.world.entity.projectile

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import java.util.function.Predicate

object ProjectileUtilRemap {
    @JvmStatic
    fun getWeaponHoldingHand(livingEntity: LivingEntity, itemPredicate: Predicate<Item>): InteractionHand {
        return if (itemPredicate.test(livingEntity.mainHandItem.item))
            InteractionHand.MAIN_HAND
        else
            InteractionHand.OFF_HAND
    }
}