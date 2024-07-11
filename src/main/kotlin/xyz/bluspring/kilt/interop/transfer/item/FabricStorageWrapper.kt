package xyz.bluspring.kilt.interop.transfer.item

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext

class FabricStorageWrapper(val wrapped: Storage<ItemVariant>) : SlottedStorage<ItemVariant> {
    override fun iterator(): MutableIterator<StorageView<ItemVariant>> {
        return wrapped.iterator()
    }

    override fun getSlotCount(): Int {
        return wrapped.toList().size
    }

    override fun getSlot(slot: Int): SingleSlotStorage<ItemVariant> {
        return FabricSingleStorageWrapper(wrapped.toList()[slot])
    }

    override fun extract(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        return wrapped.extract(resource, maxAmount, transaction)
    }

    override fun insert(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        return wrapped.insert(resource, maxAmount, transaction)
    }

    private inner class FabricSingleStorageWrapper(val wrapped: StorageView<ItemVariant>) : SingleSlotStorage<ItemVariant> {
        override fun insert(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
            return 0L
        }

        override fun extract(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
            return wrapped.extract(resource, maxAmount, transaction)
        }

        override fun isResourceBlank(): Boolean {
            return wrapped.isResourceBlank
        }

        override fun getResource(): ItemVariant {
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