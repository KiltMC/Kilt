package xyz.bluspring.kilt.interop.transfer.item

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.world.item.ItemStack

class FabricStorageWrapper(val wrapped: Storage<ItemVariant>) : SlotExposedStorage {
    override fun iterator(): MutableIterator<StorageView<ItemVariant>> {
        return wrapped.iterator()
    }

    override fun extract(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        return wrapped.extract(resource, maxAmount, transaction)
    }

    override fun insert(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        return wrapped.insert(resource, maxAmount, transaction)
    }

    override fun getStackInSlot(slot: Int): ItemStack? {
        val item = wrapped.toList()[slot]
        return item.resource.toStack(item.amount.toInt())
    }

    override fun getSlots(): Int {
        return wrapped.toList().size
    }

    override fun getSlotLimit(slot: Int): Int {
        return wrapped.toList()[slot].capacity.toInt()
    }

    override fun isItemValid(
        slot: Int,
        resource: ItemVariant?,
        amount: Long
    ): Boolean {
        val item = wrapped.toList()[slot]
        return item.resource == resource
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