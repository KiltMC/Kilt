package xyz.bluspring.kilt.compat.curios_trinkets.trinkets

import com.google.common.collect.Multimap
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketEnums
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ItemStack
import top.theillusivec4.curios.api.type.capability.ICurio
import xyz.bluspring.kilt.compat.curios_trinkets.curios.slotContextFromTrinkets
import java.util.*

class CuriosTrinket(val curio: ICurio) : Trinket {
    override fun canEquip(stack: ItemStack?, slot: SlotReference, entity: LivingEntity): Boolean {
        return curio.canEquip(slotContextFromTrinkets(slot, entity))
    }

    override fun canUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity): Boolean {
        return curio.canUnequip(slotContextFromTrinkets(slot, entity))
    }

    override fun getDropRule(stack: ItemStack, slot: SlotReference, entity: LivingEntity): TrinketEnums.DropRule {
        // TODO: is this correct?
        return dropRuleFromCurios(curio.getDropRule(slotContextFromTrinkets(slot, entity), entity.lastDamageSource, 0, false))
    }

    override fun getModifiers(
        stack: ItemStack,
        slot: SlotReference,
        entity: LivingEntity,
        uuid: UUID
    ): Multimap<Attribute, AttributeModifier> {
        return curio.getAttributeModifiers(slotContextFromTrinkets(slot, entity), uuid)
    }

    override fun onBreak(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        curio.curioBreak(slotContextFromTrinkets(slot, entity))
    }

    override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        curio.onEquip(slotContextFromTrinkets(slot, entity), slot.inventory.getItem(slot.index))
    }

    override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        curio.onUnequip(slotContextFromTrinkets(slot, entity), slot.inventory.getItem(slot.index))
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        curio.curioTick(slotContextFromTrinkets(slot, entity))
    }
}