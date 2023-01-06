package net.minecraftforge.fluids

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.material.Fluid

open class FluidStack : io.github.fabricators_of_create.porting_lib.util.FluidStack {
    constructor(fluid: Fluid, amount: Int) : super(fluid, amount.toLong())
    constructor(fluid: Fluid, amount: Int, nbt: CompoundTag) : super(fluid, amount.toLong(), nbt)
    constructor(stack: FluidStack, amount: Int) : super(stack, amount.toLong())

    fun containsFluid(other: FluidStack): Boolean {
        return isFluidEqual(other) && amount >= other.amount
    }

    override fun copy(): FluidStack {
        return super.copy() as FluidStack
    }

    fun getChildTag(childName: String): CompoundTag? {
        if (tag == null)
            return null

        return tag!!.getCompound(childName)
    }

    fun getOrCreateChildTag(childName: String): CompoundTag {
        val child = orCreateTag.getCompound(childName)

        if (!tag!!.contains(childName, Tag.TAG_COMPOUND.toInt())) {
            tag!!.put(childName, child)
        }

        return child
    }

    protected fun updateEmpty() {
    }

    companion object {
        @JvmStatic
        fun areFluidStackTagsEqual(stack1: FluidStack, stack2: FluidStack): Boolean {
            return stack1.isFluidEqual(stack2)
        }
    }
}