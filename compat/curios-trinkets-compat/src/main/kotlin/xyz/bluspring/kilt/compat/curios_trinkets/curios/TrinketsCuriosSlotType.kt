package xyz.bluspring.kilt.compat.curios_trinkets.curios

import dev.emi.trinkets.api.SlotType
import net.minecraft.resources.ResourceLocation
import top.theillusivec4.curios.api.type.ISlotType
import top.theillusivec4.curios.api.type.capability.ICurio

class CuriosTrinketsSlotType(val deferred: SlotType) : ISlotType {
    override fun getIdentifier(): String {
        return TRINKETS_SLOT_TO_CURIOS_SLOT[deferred.group]?.get(deferred.name) ?: deferred.name
    }

    override fun getIcon(): ResourceLocation? {
        return deferred.icon
    }

    override fun getOrder(): Int {
        return deferred.order
    }

    override fun getSize(): Int {
        return deferred.amount
    }

    override fun useNativeGui(): Boolean {
        return true
    }

    override fun hasCosmetic(): Boolean {
        return true
    }

    override fun canToggleRendering(): Boolean {
        return false
    }

    override fun getDropRule(): ICurio.DropRule {
        return dropRuleFromTrinkets(deferred.dropRule)
    }

    override fun compareTo(other: ISlotType): Int {
        return if (order == other.order) {
            this.identifier.compareTo(other.identifier)
        } else if (order > other.order) {
            1
        } else {
            -1
        }
    }
}