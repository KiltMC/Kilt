package net.minecraftforge.common.crafting.conditions

import com.google.gson.JsonObject
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import com.mojang.serialization.DynamicOps
import io.github.fabricators_of_create.porting_lib.crafting.CraftingHelper
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

interface ICondition {
    val ID: ResourceLocation
    fun test(context: IContext): Boolean

    interface IContext {
        fun <T> getTag(key: TagKey<T>): Collection<Holder<T>> {
            return getAllTags(key.registry).getOrDefault(key.location, setOf())
        }

        fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>>

        companion object {
            @JvmField
            val EMPTY = object : IContext {
                override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> {
                    return mapOf()
                }
            }

            @JvmField
            val TAGS_INVALID = object : IContext {
                override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> {
                    throw UnsupportedOperationException("Usage of tag-based conditions is not permitted in this context!")
                }
            }
        }
    }

    companion object {
        @JvmField
        val DECODER = object : Decoder<Boolean> {
            override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<Boolean, T>> {
                if (input is JsonObject && input.has("forge:conditions")) {
                    return try {
                        val result = CraftingHelper.processConditions(input, "forge:conditions")
                        DataResult.success(Pair.of(result, input))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        DataResult.success(Pair.of(false, input))
                    }
                }

                return DataResult.success(Pair.of(true, input))
            }
        }
    }
}