package xyz.bluspring.kilt.interop.transfer.fluid

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.capability.IFluidHandlerItem

class FabricFluidItemStorageCapability(storage: Storage<FluidVariant>, val stack: ItemStack) : FabricFluidStorageCapability(storage), IFluidHandlerItem {
    override fun getContainer(): ItemStack {
        return stack
    }
}