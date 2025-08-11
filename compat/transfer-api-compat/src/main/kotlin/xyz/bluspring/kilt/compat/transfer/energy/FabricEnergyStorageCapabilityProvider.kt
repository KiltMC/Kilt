package xyz.bluspring.kilt.compat.transfer.energy

import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.common.capabilities.Capability
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider
import net.neoforged.neoforge.common.util.LazyOptional
import team.reborn.energy.api.EnergyStorage
import java.util.*

class FabricEnergyStorageCapabilityProvider(val blockEntity: BlockEntity) : ICapabilityProvider {
    private val capabilityCache = Collections.synchronizedMap(mutableMapOf<EnergyStorage, FabricEnergyStorageCapability>())

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ENERGY) {
            val fabricStorage = EnergyStorage.SIDED.getProvider(blockEntity.blockState.block) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(blockEntity.level!!, blockEntity.blockPos, blockEntity.blockState, blockEntity, side) ?: return LazyOptional.empty()

            // Ignore our own storage
            if (storage is ForgeEnergyStorage)
                return LazyOptional.empty()

            return LazyOptional.of { capabilityCache.computeIfAbsent(storage) { s -> FabricEnergyStorageCapability(s) } }.cast()
        }

        return LazyOptional.empty()
    }
}