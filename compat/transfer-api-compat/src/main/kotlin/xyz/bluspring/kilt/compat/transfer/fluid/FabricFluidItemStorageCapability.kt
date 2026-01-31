package xyz.bluspring.kilt.compat.transfer.fluid

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem

class FabricFluidItemStorageCapability(storage: Storage<FluidVariant>, val stack: ItemStack) : FabricFluidStorageCapability(storage), IFluidHandlerItem {
    override fun getContainer(): ItemStack {
        return stack
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        TODO("Not yet implemented")
    }

    override fun fill(
        resource: FluidStack,
        action: IFluidHandler.FluidAction
    ): Int {
        TODO("Not yet implemented")
    }

    override fun drain(
        resource: FluidStack,
        action: IFluidHandler.FluidAction
    ): FluidStack {
        TODO("Not yet implemented")
    }

    override fun drain(
        maxDrain: Int,
        action: IFluidHandler.FluidAction
    ): FluidStack {
        TODO("Not yet implemented")
    }
}