package xyz.bluspring.kilt.compat.curios_trinkets.trinkets

import dev.emi.trinkets.api.*
import top.theillusivec4.curios.api.CuriosApi
import top.theillusivec4.curios.api.SlotContext
import top.theillusivec4.curios.api.type.ISlotType
import top.theillusivec4.curios.api.type.capability.ICurio

val CURIOS_SLOT_TO_TRINKETS_SLOT = mapOf(
    "curio" to listOf("*"),
    "back" to listOf("chest/back"),
    "belt" to listOf("legs/belt"),
    "body" to listOf("chest/cape"),
    "bracelet" to listOf("hand/ring", "offhand/ring"),
    "charm" to listOf(), // TODO: how??
    "head" to listOf("head/hat"),
    "hands" to listOf("hand/glove", "offhand/glove"),
    "necklace" to listOf("chest/necklace"),
    "ring" to listOf("hand/ring", "offhand/ring")
)

fun dropRuleFromCurios(dropRule: ICurio.DropRule): TrinketEnums.DropRule {
    return when (dropRule) {
        ICurio.DropRule.DEFAULT -> TrinketEnums.DropRule.DEFAULT
        ICurio.DropRule.ALWAYS_DROP -> TrinketEnums.DropRule.DROP
        ICurio.DropRule.ALWAYS_KEEP -> TrinketEnums.DropRule.KEEP
        ICurio.DropRule.DESTROY -> TrinketEnums.DropRule.DESTROY
    }
}

fun trinketsInventoryFromCurios(context: SlotContext): TrinketInventory? {
    val slots = CuriosApi.getEntitySlots(context.entity())
    val inventory = TrinketsApi.getTrinketComponent(context.entity).orElseThrow().inventory

    val slotType = slots[context.identifier()] ?: return null

    return null
}

fun slotReferenceFromCurios(context: SlotContext): SlotReference? {
    return SlotReference(trinketsInventoryFromCurios(context), context.index())
}

fun slotTypeFromCurios(name: String, type: ISlotType): List<SlotType> {
    val names = CURIOS_SLOT_TO_TRINKETS_SLOT[name]?.map { it.split("/") } ?: emptyList()
    val types = mutableListOf<SlotType>()

    for ((group, name) in names) {
        types.add(SlotType(
            group, name, type.order, type.size, type.icon,
            emptySet(), emptySet(), emptySet(),
            dropRuleFromCurios(type.dropRule),
        ))
    }

    return types
}