package xyz.bluspring.kilt.remaps.advancements

import com.google.gson.JsonObject
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.critereon.DeserializationContext
import net.minecraftforge.common.crafting.ConditionalAdvancement
import net.minecraftforge.common.crafting.conditions.ICondition

open class AdvancementBuilderRemap : Advancement.Builder() {
    companion object {
        @JvmStatic
        fun fromJson(
            json: JsonObject,
            deserializationContext: DeserializationContext,
            context: ICondition.IContext
        ): Advancement.Builder? {
            val processedConditional = ConditionalAdvancement.processConditional(json, context) ?: return null
            return fromJson(processedConditional, deserializationContext)
        }
    }
}