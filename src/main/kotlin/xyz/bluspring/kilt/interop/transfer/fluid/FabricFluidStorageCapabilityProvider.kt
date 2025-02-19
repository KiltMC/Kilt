package xyz.bluspring.kilt.interop.transfer.fluid

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class FabricFluidStorageCapabilityProvider(val blockEntity: BlockEntity) : ICapabilityProvider {
    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            val fabricStorage = FluidStorage.SIDED.getProvider(blockEntity.blockState.block) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(blockEntity.level!!, blockEntity.blockPos, blockEntity.blockState, blockEntity, side) ?: return LazyOptional.empty()

            if (storage is ForgeFluidStorage)
                return LazyOptional.empty()

            return LazyOptional.of { FabricFluidStorageCapability(storage) }.cast()
        }

        return LazyOptional.empty()
    }
}