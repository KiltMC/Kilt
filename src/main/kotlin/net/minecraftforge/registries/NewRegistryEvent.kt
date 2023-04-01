package net.minecraftforge.registries

import com.mojang.serialization.Lifecycle
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent
import java.util.function.Consumer
import java.util.function.Supplier

class NewRegistryEvent : Event(), IModBusEvent {
    private val registries = mutableListOf<Triple<RegistryBuilder<*>, RegistryHolder<*>, Consumer<IForgeRegistry<*>>?>>()

    fun <V> create(builder: RegistryBuilder<V>): Supplier<IForgeRegistry<V>?> {
        return create(builder, null)
    }

    fun <V> create(builder: RegistryBuilder<V>, onFill: Consumer<IForgeRegistry<V>>?): Supplier<IForgeRegistry<V>?> {
        val holder = RegistryHolder<V>()
        registries.add(Triple(builder, holder, onFill as Consumer<IForgeRegistry<*>>?))

        return holder
    }

    internal fun fill() {
        registries.forEach { (builder, holder, consumer) ->
            val registry = builder.create() as ForgeRegistry<*>

            if (builder.dataPackRegistryData != null) {
                if (!BuiltinRegistries.REGISTRY.containsKey(registry.registryName)) {
                    DataPackRegistriesHooks.addRegistryCodec(builder.dataPackRegistryData!!)

                    val wrapper = registry.wrapper
                    if (wrapper != null)
                        (BuiltinRegistries.REGISTRY as WritableRegistry<Registry<*>>).register(registry.registryKey as ResourceKey<Registry<*>>, wrapper, Lifecycle.experimental())
                }
            } else if (builder.hasWrapper && !Registry.REGISTRY.containsKey(registry.registryName)) {
                val wrapper = registry.wrapper
                if (wrapper != null)
                    (Registry.REGISTRY as WritableRegistry<Registry<*>>).register(registry.registryKey as ResourceKey<Registry<*>>, wrapper, Lifecycle.experimental())
            }

            holder.registry = registry
            consumer?.accept(registry)
        }
    }

    companion object {
        val forgeRegistries = mutableMapOf<ResourceLocation, IForgeRegistry<*>>()
    }

    private class RegistryHolder<V> : Supplier<IForgeRegistry<V>?> {
        var registry: IForgeRegistry<*>? = null

        override fun get(): IForgeRegistry<V>? {
            return registry as IForgeRegistry<V>?
        }
    }
}