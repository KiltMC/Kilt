package xyz.bluspring.kilt.remaps.advancements

import com.google.gson.JsonObject
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.critereon.DeserializationContext
import net.minecraftforge.common.crafting.ConditionalAdvancement
import net.minecraftforge.common.crafting.conditions.ICondition

object AdvancementBuilderRemap {
    @JvmStatic
    fun fromJson(json: JsonObject, deserializationContext: DeserializationContext, context: common.crafting.conditions.ICondition.IContext): Advancement.Builder? {
        val processedConditional = common.crafting.ConditionalAdvancement.processConditional(json, context) ?: return null
        return Advancement.Builder.fromJson(processedConditional, deserializationContext)
    }
}