package net.minecraftforge.common

import net.minecraft.world.item.ItemStack

object ForgeHooks {
    @JvmStatic
    fun canContinueUsing(from: ItemStack, to: ItemStack): Boolean {
        if (!from.isEmpty && !to.isEmpty)
            return from.item.canContinueUsing(from, to)

        return false
    }
}