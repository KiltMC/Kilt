package xyz.bluspring.kilt.interop.transfer.item

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler

class ForgeSlottedStorage(val handler: IItemHandler) : SlottedStorage<ItemVariant> {
    override fun iterator(): MutableIterator<StorageView<ItemVariant>> {
        // TODO: make this an actually good iterator
        return object : MutableIterator<StorageView<ItemVariant>> {
            var currentIndex = 0

            override fun hasNext(): Boolean {
                return currentIndex + 1 < handler.slots
            }

            override fun next(): StorageView<ItemVariant> {
                return ForgeSingleSlotStorage(currentIndex++)
            }

            override fun remove() {
                currentIndex++
            }
        }
    }

    override fun getSlotCount(): Int {
        return handler.slots
    }

    override fun getSlot(slot: Int): SingleSlotStorage<ItemVariant> {
        return ForgeSingleSlotStorage(slot)
    }

    override fun extract(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long {
        var currentAmount = maxAmount
        for (slot in 0 until handler.slots) {
            val extracted = getSlot(slot).extract(resource, currentAmount, transaction)
            currentAmount -= extracted

            if (currentAmount <= 0)
                return maxAmount
        }

        return maxAmount - currentAmount
    }

    override fun insert(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long {
        var currentAmount = maxAmount
        for (slot in 0 until handler.slots) {
            val inserted = getSlot(slot).insert(resource, currentAmount, transaction)
            currentAmount -= inserted

            if (currentAmount <= 0)
                return maxAmount
        }

        return maxAmount - currentAmount
    }

    inner class ForgeSingleSlotStorage(val slot: Int) : SingleSlotStorage<ItemVariant> {
        override fun insert(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long {
            val snapshot = ForgeStackInsertSnapshot(resource.toStack(maxAmount.toInt()))

            snapshot.updateSnapshots(transaction)
            val notInserted = handler.insertItem(slot, resource.toStack(maxAmount.toInt()), true)

            return maxAmount - notInserted.count
        }

        override fun extract(resource: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long {
            val snapshot = ForgeStackExtractSnapshot(maxAmount)

            snapshot.updateSnapshots(transaction)
            return handler.extractItem(slot, maxAmount.toInt(), true).count.toLong()
        }

        override fun isResourceBlank(): Boolean {
            return handler.getStackInSlot(slot).isEmpty
        }

        override fun getResource(): ItemVariant {
            return ItemVariant.of(handler.getStackInSlot(slot))
        }

        override fun getAmount(): Long {
            return handler.getStackInSlot(slot).count.toLong()
        }

        override fun getCapacity(): Long {
            return handler.getSlotLimit(slot).toLong()
        }

        inner class ForgeStackInsertSnapshot(var stack: ItemStack) : SnapshotParticipant<ItemStack>() {
            val original = stack.copy()

            override fun createSnapshot(): ItemStack {
                return stack
            }

            override fun readSnapshot(snapshot: ItemStack) {
                stack = snapshot
            }

            override fun onFinalCommit() {
                handler.insertItem(slot, original, false)
            }
        }

        inner class ForgeStackExtractSnapshot(var maxAmount: Long) : SnapshotParticipant<Long>() {
            val original = maxAmount

            override fun createSnapshot(): Long {
                return maxAmount
            }

            override fun readSnapshot(snapshot: Long) {
                maxAmount = snapshot
            }

            override fun onFinalCommit() {
                handler.extractItem(slot, original.toInt(), false)
            }
        }
    }
}