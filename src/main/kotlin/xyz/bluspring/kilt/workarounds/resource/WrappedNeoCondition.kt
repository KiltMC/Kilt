package xyz.bluspring.kilt.workarounds.resource

import com.mojang.serialization.MapCodec
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.neoforged.neoforge.common.conditions.ICondition
import net.neoforged.neoforge.common.conditions.ICondition.IContext
import net.neoforged.neoforge.registries.NeoForgeRegistries

data class WrappedNeoCondition<C : ICondition>(val wrapped: C) : ResourceCondition {
    override fun getType(): ResourceConditionType<*> {
        return getOrRegister(this.wrapped.codec())
    }

    override fun test(registryLookup: HolderLookup.Provider?): Boolean {
        return this.wrapped.test(
            if (registryLookup == null)
                IContext.EMPTY
            else
                object : IContext {
                    override fun <T> getAllTags(registry: ResourceKey<out Registry<T>>): Map<ResourceLocation, Collection<Holder<T>>> {
                        return registryLookup.lookupOrThrow(registry).listTags().toList()
                            .associate { it.key().location to it.toList() }
                    }

                    override fun <T> getTag(key: TagKey<T>): Collection<Holder<T>> {
                        return registryLookup.lookupOrThrow(key.registry)
                            .getOrThrow(key)
                            .toList()
                    }
                }
        )
    }

    companion object {
        @JvmField val CODEC_TO_TYPE_REGISTRY: MutableMap<MapCodec<out ICondition>, ResourceConditionType<*>> = mutableMapOf()

        @JvmStatic @JvmOverloads
        fun <C : ICondition> getOrRegister(codec: MapCodec<C>, id: ResourceLocation = NeoForgeRegistries.CONDITION_SERIALIZERS.getKey(codec)
            ?: throw IllegalStateException("Condition $codec was not registered to NeoForge!")
        ): ResourceConditionType<WrappedNeoCondition<C>> {
            if (CODEC_TO_TYPE_REGISTRY.contains(codec))
                return CODEC_TO_TYPE_REGISTRY[codec]!! as ResourceConditionType<WrappedNeoCondition<C>>

            val type = ResourceConditionType.create(id, codec.xmap(::WrappedNeoCondition, WrappedNeoCondition<C>::wrapped))
            CODEC_TO_TYPE_REGISTRY[codec] = type
            ResourceConditions.register(type)
            return type
        }
    }
}
