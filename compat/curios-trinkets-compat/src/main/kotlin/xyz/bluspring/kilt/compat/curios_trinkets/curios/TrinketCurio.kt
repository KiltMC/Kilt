package xyz.bluspring.kilt.compat.curios_trinkets.curios

import dev.emi.trinkets.api.Trinket
import net.minecraft.world.item.ItemStack
import top.theillusivec4.curios.api.SlotContext
import top.theillusivec4.curios.api.type.capability.ICurio
import xyz.bluspring.kilt.compat.curios_trinkets.trinkets.slotReferenceFromCurios

class TrinketCurio(private val stack: ItemStack, val trinket: Trinket) : ICurio {
    override fun getStack(): ItemStack {
        return stack
    }

    override fun canEquip(slotContext: SlotContext): Boolean {
        return trinket.canEquip(stack, slotReferenceFromCurios(slotContext), slotContext.entity)
    }

    override fun canEquipFromUse(slotContext: SlotContext?): Boolean {
        return super.canEquipFromUse(slotContext)
    }

    override fun curioBreak(slotContext: SlotContext?) {
        super.curioBreak(slotContext)
    }

    override fun canUnequip(slotContext: SlotContext?): Boolean {
        return super.canUnequip(slotContext)
    }
}