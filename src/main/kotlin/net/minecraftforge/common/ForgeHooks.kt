package net.minecraftforge.common

import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.FurnaceBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.extensions.IForgeItem
import net.minecraftforge.common.extensions.IForgeItemStack
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.registries.ForgeRegistries

object ForgeHooks {
    private val VANILLA_BURNS = mutableMapOf<Holder.Reference<Item>, Int>()

    @JvmStatic
    fun canContinueUsing(from: ItemStack, to: ItemStack): Boolean {
        if (!from.isEmpty && !to.isEmpty)
            return (from.item as IForgeItem).canContinueUsing(from, to)

        return false
    }

    @JvmStatic
    fun onCropsGrowPre(level: Level, pos: BlockPos, state: BlockState, def: Boolean): Boolean {
        val ev = BlockEvent.CropGrowEvent.Pre(level, pos, state)
        MinecraftForge.EVENT_BUS.post(ev)

        return (ev.result == Event.Result.ALLOW) || (ev.result == Event.Result.DEFAULT && def)
    }

    @JvmStatic
    fun onCropsGrowPost(level: Level, pos: BlockPos, state: BlockState) {
        MinecraftForge.EVENT_BUS.post(BlockEvent.CropGrowEvent.Post(level, pos, state, level.getBlockState(pos)))
    }

    @JvmStatic
    @Synchronized
    fun updateBurns() {
        VANILLA_BURNS.clear()
        FurnaceBlockEntity.getFuel().entries.forEach {
            VANILLA_BURNS[ForgeRegistries.ITEMS.getDelegateOrThrow(it.key)] = it.value
        }
    }

    @JvmStatic
    fun getBurnTime(stack: ItemStack, recipeType: RecipeType<*>): Int {
        return if (stack.isEmpty)
            0
        else {
            val item = stack.item
            val burnTime = (stack as IForgeItemStack).getBurnTime(recipeType)
            ForgeEventFactory.getItemBurnTime(stack,
                if (burnTime == -1)
                    VANILLA_BURNS.getOrDefault(ForgeRegistries.ITEMS.getDelegateOrThrow(item), 0)
                else burnTime,
                recipeType
            )
        }
    }

    @JvmStatic
    fun isCorrectToolForDrops(state: BlockState, player: Player): Boolean {
        if (!state.requiresCorrectToolForDrops())
            return ForgeEventFactory.doPlayerHarvestCheck(player, state, true)

        return player.hasCorrectToolForDrops(state)
    }
}