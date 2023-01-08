package net.minecraftforge.fluids

import net.minecraft.world.item.ItemStack

class FluidActionResult private constructor(
    @JvmField
    val success: Boolean,
    @JvmField
    val result: ItemStack
) {
    constructor(result: ItemStack) : this(true, result)

    fun getResult(): ItemStack {
        return result
    }

    fun isSuccess(): Boolean {
        return success
    }

    companion object {
        @JvmStatic
        val FAILURE = FluidActionResult(false, ItemStack.EMPTY)
    }
}