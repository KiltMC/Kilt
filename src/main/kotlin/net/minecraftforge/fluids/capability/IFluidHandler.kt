package net.minecraftforge.fluids.capability

import net.minecraftforge.fluids.FluidStack

interface IFluidHandler {
    fun drain(maxDrain: Int, action: FluidAction): FluidStack
    fun drain(resource: FluidStack, action: FluidAction): FluidStack
    fun fill(resource: FluidStack, action: FluidAction): Int
    fun getFluidInTank(tank: Int): FluidStack
    fun getTankCapacity(tank: Int): Int
    val tanks: Int
    fun isFluidValid(tank: Int, stack: FluidStack): Boolean

    enum class FluidAction {
        EXECUTE, SIMULATE;

        fun execute(): Boolean {
            return this == EXECUTE
        }

        fun simulate(): Boolean {
            return this == SIMULATE
        }
    }
}