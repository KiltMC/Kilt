package net.minecraftforge.fluids

import net.minecraftforge.fluids.capability.IFluidHandler

interface IFluidTank {
    val fluid: FluidStack
    val fluidAmount: Int
    val capacity: Int
    fun isFluidValid(stack: FluidStack): Boolean
    fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int
    fun drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack
    fun drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack
}