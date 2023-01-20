package net.minecraftforge.registries

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent
import java.util.function.Consumer
import java.util.function.Supplier

class NewRegistryEvent : Event(), IModBusEvent {
    private val registries = mutableMapOf<RegistryBuilder<*>, Consumer<IForgeRegistry<*>>>()

    fun <V> create(builder: RegistryBuilder<V>): Supplier<IForgeRegistry<V>?> {
        return create(builder, null)
    }

    fun <V> create(builder: RegistryBuilder<V>, onFill: Consumer<IForgeRegistry<V>>?): Supplier<IForgeRegistry<V>?> {
        if (onFill != null)
            registries[builder] = onFill as Consumer<IForgeRegistry<*>> // why

        return Supplier { null }
    }

    internal fun init() {
        registries.forEach { (builder, consumer) ->
            val registry = if (forgeRegistries.contains(builder.name))
                ForgeRegistry(RegistryManager.ACTIVE, builder.name, builder)
            else forgeRegistries[builder.name]!!

            consumer.accept(registry)
        }
    }

    companion object {
        val forgeRegistries = mutableMapOf<ResourceLocation, IForgeRegistry<*>>()
    }
}