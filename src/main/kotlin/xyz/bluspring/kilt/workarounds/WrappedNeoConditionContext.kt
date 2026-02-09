package xyz.bluspring.kilt.workarounds

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.neoforged.neoforge.common.conditions.ICondition
import io.github.fabricators_of_create.porting_lib.resources.conditions.ICondition as PortingLibCondition

class WrappedNeoConditionContext(val neoContext: ICondition.IContext) : PortingLibCondition.IContext {
    override fun <T> getTag(key: TagKey<T>): Collection<Holder<T>> {
        return neoContext.getTag(key)
    }

    override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> {
        return neoContext.getAllTags(registry)
    }
}