package xyz.bluspring.kilt.compat.transfer.energy

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraftforge.energy.IEnergyStorage
import team.reborn.energy.api.EnergyStorage
import xyz.bluspring.kilt.compat.transfer.TransferInterop

class ForgeEnergyStorage(val storage: IEnergyStorage) : EnergyStorage {
    override fun insert(maxAmount: Long, transaction: TransactionContext): Long {
        val snapshot = ForgeEnergySnapshot(true)
        snapshot.updateSnapshots(transaction)

        val inserted = storage.receiveEnergy(maxAmount.toInt() * TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY, true)
        return inserted.toLong() / TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
    }

    override fun extract(maxAmount: Long, transaction: TransactionContext): Long {
        val snapshot = ForgeEnergySnapshot(false)
        snapshot.updateSnapshots(transaction)

        val extracted = storage.extractEnergy(maxAmount.toInt() * TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY, true)
        return extracted.toLong() / TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
    }

    override fun getAmount(): Long {
        return storage.energyStored.toLong() / TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
    }

    override fun getCapacity(): Long {
        return storage.maxEnergyStored.toLong() / TransferInterop.REBORN_ENERGY_TO_FORGE_ENERGY
    }

    private inner class ForgeEnergySnapshot(val insert: Boolean) : SnapshotParticipant<Int>() {
        val original = storage.energyStored
        var current = original

        override fun createSnapshot(): Int {
            return current
        }

        override fun readSnapshot(snapshot: Int) {
            current = snapshot
        }

        override fun onFinalCommit() {
            if (insert) {
                storage.receiveEnergy(current, false)
            } else {
                storage.extractEnergy(current, false)
            }
        }
    }
}