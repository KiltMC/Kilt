package net.minecraftforge.registries

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.function.Supplier

class DeferredRegister<T> private constructor(
    private val fabricRegister: LazyRegistrar<T>
) {
    private val optionalTags = mutableMapOf<TagKey<T>, MutableSet<Supplier<T>>>()

    fun createOptionalTagKey(path: String, defaults: Set<Supplier<T>>): TagKey<T> {
        return createOptionalTagKey(ResourceLocation(fabricRegister.mod_id, path), defaults)
    }

    fun createOptionalTagKey(location: ResourceLocation, defaults: Set<Supplier<T>>): TagKey<T> {
        val tagKey = createTagKey(location)

        addOptionalTagDefaults(tagKey, defaults)
        return tagKey
    }

    fun createTagKey(path: String): TagKey<T> {
        return createTagKey(ResourceLocation(fabricRegister.mod_id, path))
    }

    fun createTagKey(location: ResourceLocation): TagKey<T> {
        return TagKey.create(fabricRegister.registryKey, location)
    }

    fun addOptionalTagDefaults(name: TagKey<T>, defaults: Set<Supplier<T>>) {
        if (!optionalTags.contains(name))
            optionalTags[name] = mutableSetOf()

        optionalTags[name]!!.addAll(defaults)
    }

    fun register(bus: IEventBus) {
        bus.register(EventDispatcher(this))
        bus.addListener(this::createRegistry)
    }

    class EventDispatcher(private val register: DeferredRegister<*>) {
        @SubscribeEvent
        fun handleEvent(event: RegisterEvent) {
            register.fabricRegister.register()
        }
    }

    val entries: Collection<RegistryObject<T>>
        get() {
            return fabricRegister.entries.map {
                RegistryObject(it)
            }
        }

    val registryKey: ResourceKey<out Registry<T>>
        get() {
            return fabricRegister.registryKey
        }

    val registryName: ResourceLocation
        get() {
            return fabricRegister.registryKey.location()
        }

    private fun createRegistry(event: NewRegistryEvent) {
        event.create(RegistryBuilder()) {
            val tagManager = it.tags() ?: return@create

            optionalTags.forEach(tagManager::addOptionalTagDefaults)
        }
    }

    fun makeRegistry(sup: Supplier<RegistryBuilder<T>>): Supplier<IForgeRegistry<T>> {
        return Supplier {
            ForgeRegistry(this.registryKey.location(), sup.get())
        }
    }

    fun <I : T> register(name: String, sup: Supplier<out I>): RegistryObject<I> {
        return RegistryObject(fabricRegister.register(name, sup))
    }

    companion object {
        @JvmStatic
        fun <B> create(key: ResourceKey<out Registry<B>>, modid: String): DeferredRegister<B> {
            return DeferredRegister(LazyRegistrar.create(key, modid))
        }

        @JvmStatic
        fun <B> create(registryName: ResourceLocation, modid: String): DeferredRegister<B> {
            return DeferredRegister(LazyRegistrar.create(registryName, modid))
        }

        @JvmStatic
        fun <B> create(reg: IForgeRegistry<B>, modid: String): DeferredRegister<B> {
            return DeferredRegister(LazyRegistrar.create(reg.registryKey, modid))
        }

        @JvmStatic
        fun <B> createOptional(key: ResourceKey<out Registry<B>>, modid: String): DeferredRegister<B> {
            return DeferredRegister(LazyRegistrar.create(key, modid))
        }

        @JvmStatic
        fun <B> createOptional(registryName: ResourceLocation, modid: String): DeferredRegister<B> {
            return DeferredRegister(LazyRegistrar.create(registryName, modid))
        }
    }
}