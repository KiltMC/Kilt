package net.minecraftforge.event.entity.player

import com.google.common.base.Preconditions
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.item.ItemStack
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class ItemFishedEvent(stacks: List<ItemStack>, var rodDamage: Int, hook: FishingHook) : PlayerEvent(hook.playerOwner!!) {
    val drops = stacks
    val hookEntity = hook

    fun damageRodBy(rodDamage: Int) {
        Preconditions.checkArgument(rodDamage >= 0)
        this.rodDamage = rodDamage
    }
}