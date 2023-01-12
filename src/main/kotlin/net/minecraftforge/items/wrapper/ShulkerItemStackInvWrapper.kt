package net.minecraftforge.items.wrapper

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.ContainerHelper
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.ShulkerBoxBlock
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.IItemHandlerModifiable


class ShulkerItemStackInvWrapper(val stack: ItemStack) : IItemHandlerModifiable, ICapabilityProvider {
    override val slots = 27
    private val holder = LazyOptional.of { this@ShulkerItemStackInvWrapper }

    private var itemStacksCache: NonNullList<ItemStack>? = null
    private var cachedTag: CompoundTag? = null
    private var itemList: NonNullList<ItemStack>
        get() {
            val rootTag = BlockItem.getBlockEntityData(stack)
            if (cachedTag == null || rootTag != cachedTag)
                itemStacksCache = refreshItemList(rootTag)

            return itemStacksCache!!
        }
        set(value) {
            val rootTag = ContainerHelper.saveAllItems(BlockItem.getBlockEntityData(stack) ?: CompoundTag(), value)
            BlockItem.setBlockEntityData(stack, BlockEntityType.SHULKER_BOX, rootTag)
            cachedTag = rootTag
        }

    private fun refreshItemList(tag: CompoundTag?): NonNullList<ItemStack> {
        val itemStacks = NonNullList.withSize(slots, ItemStack.EMPTY)
        if (tag != null && tag.contains("Items", CompoundTag.TAG_LIST.toInt()))
            ContainerHelper.loadAllItems(tag, itemStacks)

        cachedTag = tag
        return itemStacks
    }

    private fun validateSlotIndex(slot: Int) {
        if (slot < 0 || slot >= slots)
            throw RuntimeException("Slot $slot not in valid range [0,$slots)")
    }

    override fun getStackInSlot(slot: Int): ItemStack {
        validateSlotIndex(slot)
        return itemList[slot]
    }

    override fun setStackInSlot(slot: Int, stack: ItemStack) {
        validateSlotIndex(slot)
        if (!isItemValid(slot, stack))
            throw RuntimeException("Invalid stack $stack for slot $slot)")

        val itemStacks: NonNullList<ItemStack> = itemList
        itemStacks[slot] = stack

        itemList = itemStacks
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (stack.isEmpty) return ItemStack.EMPTY

        if (!isItemValid(slot, stack)) return stack

        validateSlotIndex(slot)

        val itemStacks: NonNullList<ItemStack> = itemList

        val existing = itemStacks[slot]

        var limit = getSlotLimit(slot).coerceAtMost(stack.maxStackSize)

        if (!existing.isEmpty) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) return stack
            limit -= existing.count
        }

        if (limit <= 0) return stack

        val reachedLimit = stack.count > limit

        if (!simulate) {
            if (existing.isEmpty) {
                itemStacks[slot] = if (reachedLimit) ItemHandlerHelper.copyStackWithSize(stack, limit) else stack
            } else {
                existing.grow(if (reachedLimit) limit else stack.count)
            }
            itemList = itemStacks
        }

        return if (reachedLimit)
            ItemHandlerHelper.copyStackWithSize(stack, stack.count - limit)
        else ItemStack.EMPTY
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        val itemStacks = itemList
        if (amount == 0)
            return ItemStack.EMPTY

        validateSlotIndex(slot)

        val existing = itemStacks[slot]

        if (existing.isEmpty) return ItemStack.EMPTY

        val toExtract = amount.coerceAtMost(existing.maxStackSize)

        return if (existing.count <= toExtract) {
            if (!simulate) {
                itemStacks[slot] = ItemStack.EMPTY
                itemList = itemStacks
                existing
            } else {
                existing.copy()
            }
        } else {
            if (!simulate) {
                itemStacks[slot] = ItemHandlerHelper.copyStackWithSize(existing, existing.count - toExtract)
                itemList = itemStacks
            }
            ItemHandlerHelper.copyStackWithSize(existing, toExtract)
        }
    }

    override fun getSlotLimit(slot: Int): Int {
        return 64
    }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        return stack.item.canFitInsideContainerItems()
    }

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, holder as LazyOptional<IItemHandler>)
    }

    companion object {
        @JvmStatic
        fun createDefaultProvider(itemStack: ItemStack): ICapabilityProvider? {
            val item = itemStack.item

            // this is a lot nicer-looking than whatever the fuck Forge was doing
            if (item is BlockItem && item.block is ShulkerBoxBlock) {
                return ShulkerItemStackInvWrapper(itemStack)
            }

            return null
        }
    }
}