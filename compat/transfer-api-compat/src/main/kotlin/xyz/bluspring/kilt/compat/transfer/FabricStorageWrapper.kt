package xyz.bluspring.kilt.compat.transfer

import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext

class FabricStorageWrapper<T, V : TransferVariant<T>>(val wrapped: Storage<V>) : SlottedStorage<V> {
    override fun iterator(): MutableIterator<StorageView<V>> {
        return wrapped.iterator()
    }

    override fun getSlotCount(): Int {
        return wrapped.toList().size
    }

    override fun getSlot(slot: Int): SingleSlotStorage<V> {
        return FabricSingleStorageWrapper(wrapped.toList()[slot])
    }

    override fun extract(resource: V?, maxAmount: Long, transaction: TransactionContext?): Long {
        return wrapped.extract(resource, maxAmount, transaction)
    }

    override fun insert(resource: V?, maxAmount: Long, transaction: TransactionContext?): Long {
        return wrapped.insert(resource, maxAmount, transaction)
    }

    private class FabricSingleStorageWrapper<T, V : TransferVariant<T>>(val wrapped: StorageView<V>) : SingleSlotStorage<V> {
        override fun insert(resource: V?, maxAmount: Long, transaction: TransactionContext?): Long {
            return 0L
        }

        override fun extract(resource: V?, maxAmount: Long, transaction: TransactionContext?): Long {
            return wrapped.extract(resource, maxAmount, transaction)
        }

        override fun isResourceBlank(): Boolean {
            return wrapped.isResourceBlank
        }

        override fun getResource(): V {
            return wrapped.resource
        }

        override fun getAmount(): Long {
            return wrapped.amount
        }

        override fun getCapacity(): Long {
            return wrapped.capacity
        }

    }
}
