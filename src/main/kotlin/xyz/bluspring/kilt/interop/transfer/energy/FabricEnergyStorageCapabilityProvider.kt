package xyz.bluspring.kilt.interop.transfer.energy

import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import team.reborn.energy.api.EnergyStorage

class FabricEnergyStorageCapabilityProvider(val blockEntity: BlockEntity) : ICapabilityProvider {
    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ENERGY) {
            val fabricStorage = EnergyStorage.SIDED.getProvider(blockEntity.blockState.block) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(blockEntity.level!!, blockEntity.blockPos, blockEntity.blockState, blockEntity, side) ?: return LazyOptional.empty()

            // Ignore our own storage
            if (storage is ForgeEnergyStorage)
                return LazyOptional.empty()

            return LazyOptional.of { FabricEnergyStorageCapability(storage) }.cast()
        }

        return LazyOptional.empty()
    }
}