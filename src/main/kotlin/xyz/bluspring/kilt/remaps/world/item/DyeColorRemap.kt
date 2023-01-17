package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.DyeItem
import net.minecraft.world.item.ItemStack
import xyz.bluspring.kilt.mixin.DyeColorAccessor

object DyeColorRemap {
    @JvmStatic
    fun getColor(stack: ItemStack): DyeColor? {
        if (stack.item is DyeItem)
            return (stack.item as DyeItem).dyeColor

        DyeColorAccessor.getById().forEach {
            if (stack.`is`(it.tag))
                return it
        }

        return null
    }
}