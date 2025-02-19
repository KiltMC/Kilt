package xyz.bluspring.kilt.interop.transfer.energy

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraftforge.energy.IEnergyStorage
import team.reborn.energy.api.EnergyStorage

class ForgeEnergyStorage(val storage: IEnergyStorage) : EnergyStorage {
    // TODO: do proper conversions between the two energy types

    override fun insert(maxAmount: Long, transaction: TransactionContext): Long {
        val snapshot = ForgeEnergySnapshot(true)
        snapshot.updateSnapshots(transaction)

        val inserted = storage.receiveEnergy(maxAmount.toInt(), true)
        return inserted.toLong()
    }

    override fun extract(maxAmount: Long, transaction: TransactionContext): Long {
        val snapshot = ForgeEnergySnapshot(false)
        snapshot.updateSnapshots(transaction)

        val extracted = storage.extractEnergy(maxAmount.toInt(), true)
        return extracted.toLong()
    }

    override fun getAmount(): Long {
        return storage.energyStored.toLong()
    }

    override fun getCapacity(): Long {
        return storage.maxEnergyStored.toLong()
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
                storage.receiveEnergy(original, false)
            } else {
                storage.extractEnergy(original, false)
            }
        }
    }
}