package xyz.bluspring.kilt.compat.curios_trinkets.curios

import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketEnums
import net.minecraft.world.entity.LivingEntity
import top.theillusivec4.curios.api.SlotContext
import top.theillusivec4.curios.api.type.capability.ICurio
import top.theillusivec4.curios.common.slottype.SlotType
import dev.emi.trinkets.api.SlotType as TrinketsSlotType

val TRINKETS_SLOT_TO_CURIOS_SLOT = mapOf(
    "head" to mapOf(
        "hat" to "head"
    ),
    "chest" to mapOf(
        "back" to "back", // https://www.youtube.com/watch?v=Oy8JqaBK8aY
        "cape" to "body",
        "necklace" to "necklace"
    ),
    "hand" to mapOf(
        "glove" to "hands",
        "ring" to "ring"
    ),
    "offhand" to mapOf(
        "glove" to "hands",
        "ring" to "ring"
    ),
    "legs" to mapOf(
        "belt" to "belt"
    )
)

fun slotContextFromTrinkets(slot: SlotReference, entity: LivingEntity): SlotContext {
    val name = TRINKETS_SLOT_TO_CURIOS_SLOT[slot.inventory.slotType.group]?.get(slot.inventory.slotType.name) ?: slot.inventory.slotType.name
    return SlotContext(name, entity, slot.index, false, true)
}

fun dropRuleFromTrinkets(dropRule: TrinketEnums.DropRule): ICurio.DropRule {
    return when (dropRule) {
        TrinketEnums.DropRule.KEEP -> ICurio.DropRule.ALWAYS_KEEP
        TrinketEnums.DropRule.DROP -> ICurio.DropRule.ALWAYS_DROP
        TrinketEnums.DropRule.DESTROY -> ICurio.DropRule.DESTROY
        TrinketEnums.DropRule.DEFAULT -> ICurio.DropRule.DEFAULT
    }
}

fun slotTypeFromTrinkets(slotType: TrinketsSlotType): SlotType {
    return SlotType.Builder(TRINKETS_SLOT_TO_CURIOS_SLOT[slotType.group]?.get(slotType.name) ?: slotType.name)
        .dropRule(dropRuleFromTrinkets(slotType.dropRule))
        .order(slotType.order)
        .icon(slotType.icon)
        .renderToggle(false)
        .build()
}