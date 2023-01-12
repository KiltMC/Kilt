package net.minecraftforge.common.crafting.conditions

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagManager
import java.util.Collections

class ConditionContext(private val tagManager: TagManager) : ICondition.IContext {
    private var loadedTags: MutableMap<ResourceKey<*>, MutableMap<ResourceLocation, Collection<Holder<*>>>>? = null

    override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> {
        if (loadedTags == null) {
            val tags = tagManager.result

            if (tags.isEmpty())
                throw IllegalStateException("Tags have not been loaded yet.")

            loadedTags = mutableMapOf()
            tags.forEach {
                loadedTags!![it.key] = it.tags.toMutableMap()
            }
        }

        return loadedTags!!.getOrDefault(registry, Collections.emptyMap()).map {
            it.key to (it.value as Collection<Holder<T>>)
        }.toMap()
    }
}