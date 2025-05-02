package xyz.bluspring.kilt.compat.transfer.energy

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil
import net.minecraftforge.energy.IEnergyStorage
import team.reborn.energy.api.EnergyStorage
import xyz.bluspring.kilt.compat.transfer.TransferInterop

open class FabricEnergyStorageCapability(val storage: EnergyStorage) : IEnergyStorage {
    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        TransferUtil.getTransaction().use { transaction ->
            val inserted = storage.insert(maxReceive.toLong() / TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY, transaction)

            if (simulate)
                transaction.abort()
            else
                transaction.commit()

            return inserted.toInt() * TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
        }
    }

    override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
        TransferUtil.getTransaction().use { transaction ->
            val extracted = storage.extract(maxExtract.toLong() / TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY, transaction)

            if (simulate)
                transaction.abort()
            else
                transaction.commit()

            return extracted.toInt() * TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
        }
    }

    override fun getEnergyStored(): Int {
        return storage.amount.toInt() * TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
    }

    override fun getMaxEnergyStored(): Int {
        return storage.capacity.toInt() * TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
    }

    override fun canExtract(): Boolean {
        return storage.supportsExtraction()
    }

    override fun canReceive(): Boolean {
        return storage.supportsInsertion()
    }

}