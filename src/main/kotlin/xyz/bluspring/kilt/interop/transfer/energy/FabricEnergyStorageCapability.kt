package xyz.bluspring.kilt.interop.transfer.energy

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil
import net.minecraftforge.energy.IEnergyStorage
import team.reborn.energy.api.EnergyStorage

open class FabricEnergyStorageCapability(val storage: EnergyStorage) : IEnergyStorage {
    // TODO: do proper conversions between the two energy types

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        TransferUtil.getTransaction().use { transaction ->
            val inserted = storage.insert(maxReceive.toLong(), transaction)

            if (simulate)
                transaction.abort()
            else
                transaction.commit()

            return inserted.toInt()
        }
    }

    override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
        TransferUtil.getTransaction().use { transaction ->
            val extracted = storage.extract(maxExtract.toLong(), transaction)

            if (simulate)
                transaction.abort()
            else
                transaction.commit()

            return extracted.toInt()
        }
    }

    override fun getEnergyStored(): Int {
        return storage.amount.toInt()
    }

    override fun getMaxEnergyStored(): Int {
        return storage.capacity.toInt()
    }

    override fun canExtract(): Boolean {
        return storage.supportsExtraction()
    }

    override fun canReceive(): Boolean {
        return storage.supportsInsertion()
    }

}