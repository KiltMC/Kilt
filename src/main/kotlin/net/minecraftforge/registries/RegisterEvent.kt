package net.minecraftforge.registries

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.registries.RegisterEvent.RegisterHelper
import java.util.function.Consumer
import java.util.function.Supplier

class RegisterEvent internal constructor(
    val registryKey: ResourceKey<out Registry<*>>,
    val forgeRegistry: ForgeRegistry<*>?,
    val vanillaRegistry: Registry<*>?
) : Event(), IModBusEvent {
    private val queuedConsumers = mutableListOf<Consumer<RegisterHelper<out Any>>>()
    private var hasRanConsumers = false

    constructor() : this(EMPTY, null, null)

    fun getForgeRegistry(): IForgeRegistry<*>? {
        return forgeRegistry
    }

    fun <T : Any> register(registryKey: ResourceKey<out Registry<*>>, name: ResourceLocation, valueSupplier: Supplier<T>) {
        if (this.registryKey == registryKey) {
            if (forgeRegistry != null)
                (forgeRegistry as ForgeRegistry<T>).register(name, valueSupplier.get())
            else {
                // no double registering. please. i beg.
                if (vanillaRegistry?.containsKey(name) == true)
                    return

                Registry.register((vanillaRegistry as Registry<T>), name, valueSupplier.get())
            }
        }
    }

    fun <T : Any> register(registryKey: ResourceKey<out Registry<*>>, consumer: Consumer<RegisterHelper<T>>) {
        if (this.registryKey == registryKey) {
            if (hasRanConsumers)
                consumer.accept(RegisterHelper { name, value -> register(registryKey, name) { value } })
            else
                queuedConsumers.add(consumer as Consumer<RegisterHelper<out Any>>)
        }
    }

    fun kiltRunQueuedConsumers(registryKey: ResourceKey<out Registry<*>>) {
        queuedConsumers.forEach {
            it.accept(RegisterHelper { name, value -> register(registryKey, name) { value } })
        }
        queuedConsumers.clear()

        hasRanConsumers = true
    }

    fun interface RegisterHelper<T> {
        fun register(name: String, value: T) {
            register(ResourceLocation(ModLoadingContext.get().activeNamespace, name), value)
        }

        fun register(key: ResourceKey<T>, value: T) {
            register(key.location(), value)
        }

        fun register(name: ResourceLocation, value: T)
    }

    companion object {
        private val EMPTY = ResourceKey.create<Registry<*>>(Registry.ROOT_REGISTRY_NAME, ResourceLocation("kilt", "empty"))
    }
}