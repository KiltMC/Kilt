package xyz.bluspring.kilt.interop.transfer.item

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler

class FabricItemStorageCapability(val storage: SlotExposedStorage) : IItemHandler {
    override fun getSlots(): Int {
        return storage.slots
    }

    override fun getStackInSlot(slot: Int): ItemStack {
        return storage.getStackInSlot(slot)
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        Transaction.openOuter().use { transaction ->
            val result = storage.insertSlot(slot, ItemVariant.of(stack), stack.count.toLong(), transaction)

            if (simulate)
                transaction.abort()
            else
                transaction.commit()

            return stack.copy().apply {
                this.count = stack.count - result.toInt()
            }
        }
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        TransferUtil.getTransaction().use {
            val stack = storage.getStackInSlot(slot)

            if (stack.isEmpty)
                return ItemStack.EMPTY

            val extractedCount = storage.extract(ItemVariant.of(stack), amount.toLong(), it)
            val extracted = stack.copy().apply {
                this.count = extractedCount.toInt()
            }

            if (simulate)
                it.abort()
            else
                it.commit()

            return extracted
        }
    }

    override fun getSlotLimit(slot: Int): Int {
        return storage.getSlotLimit(slot)
    }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        return storage.isItemValid(slot, ItemVariant.of(stack), stack.count.toLong())
    }
}