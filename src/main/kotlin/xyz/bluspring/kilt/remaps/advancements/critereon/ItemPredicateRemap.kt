package xyz.bluspring.kilt.remaps.advancements.critereon

import com.google.gson.JsonObject
import net.minecraft.advancements.critereon.EnchantmentPredicate
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.advancements.critereon.NbtPredicate
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.alchemy.Potion
import org.jetbrains.annotations.Nullable

open class ItemPredicateRemap : ItemPredicate {
    constructor() : super()
    constructor(tagKey: TagKey<Item>?, set: Set<Item>?,
                ints: MinMaxBounds.Ints, ints2: MinMaxBounds.Ints,
                enchantmentPredicates: Array<EnchantmentPredicate>,
                enchantmentPredicates2: Array<EnchantmentPredicate>,
                potion: Potion?,
                nbtPredicate: NbtPredicate
    ) : super(tagKey, set, ints, ints2, enchantmentPredicates, enchantmentPredicates2, potion, nbtPredicate)

    companion object {
        private val customPredicates =
            mutableMapOf<ResourceLocation, java.util.function.Function<JsonObject, ItemPredicate>>()

        @JvmStatic
        fun register(name: ResourceLocation, deserializer: java.util.function.Function<JsonObject, ItemPredicate>) {
            customPredicates[name] = deserializer
        }

        @JvmStatic
        val predicates: Map<ResourceLocation, java.util.function.Function<JsonObject, ItemPredicate>>
            get() = customPredicates.toMap()
    }
}