package xyz.bluspring.kilt.interop.transfer.item

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler

class FabricItemStorageCapability(val storage: SlottedStorage<ItemVariant>) : IItemHandler {
    override fun getSlots(): Int {
        return storage.slotCount
    }

    override fun getStackInSlot(slot: Int): ItemStack {
        val item = storage.getSlot(slot)
        return item.resource.toStack(item.amount.toInt())
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (simulate) {
            val insertedCount = StorageUtil.simulateInsert(storage.getSlot(slot), ItemVariant.of(stack), stack.count.toLong(), null)
            return stack.copyWithCount(stack.count - insertedCount.toInt())
        }

        val insertedCount = StorageUtil.tryInsertStacking(storage.getSlot(slot), ItemVariant.of(stack), stack.count.toLong(), null)
        return stack.copyWithCount(stack.count - insertedCount.toInt())
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        val slotStorage = storage.getSlot(slot)

        if (simulate) {
            val extractedCount = StorageUtil.simulateExtract(slotStorage, slotStorage.resource, amount.toLong(), null)
            return slotStorage.resource.toStack(extractedCount.toInt())
        }

        TransferUtil.getTransaction().use {
            val extractedCount = slotStorage.extract(slotStorage.resource, amount.toLong(), it)
            val extracted =  slotStorage.resource.toStack(extractedCount.toInt())
            it.commit()

            return extracted
        }
    }

    override fun getSlotLimit(slot: Int): Int {
        return storage.getSlot(slot).capacity.toInt()
    }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        return StorageUtil.simulateInsert(storage.getSlot(slot), ItemVariant.of(stack), stack.count.toLong(), null) > 0
    }
}