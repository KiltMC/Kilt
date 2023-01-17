package xyz.bluspring.kilt.remaps.advancements.critereon

import com.google.gson.JsonObject
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.resources.ResourceLocation

object ItemPredicateRemap {
    private val customPredicates = mutableMapOf<ResourceLocation, java.util.function.Function<JsonObject, ItemPredicate>>()

    @JvmStatic
    fun register(name: ResourceLocation, deserializer: java.util.function.Function<JsonObject, ItemPredicate>) {
        customPredicates[name] = deserializer
    }

    @JvmStatic
    val predicates: Map<ResourceLocation, java.util.function.Function<JsonObject, ItemPredicate>>
        get() = customPredicates.toMap()
}