package net.minecraftforge.registries

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.kilt.injections.porting_lib.RegistryObjectInjection
import java.util.Optional
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream
// I love Kotlin and Porting Lib
import io.github.fabricators_of_create.porting_lib.util.RegistryObject as FabricRegistryObject

class RegistryObject<T> internal constructor(
    internal val fabricRegistryObject: FabricRegistryObject<T>,
    private val isOptional: Boolean = false
) : Supplier<T> {
    private var modId = "kilt"
    internal var value: Supplier<T>? = null

    private constructor(
        fabricRegistryObject: FabricRegistryObject<T>,
        modId: String
    ) : this(fabricRegistryObject) {
        this.modId = modId
    }

    override fun get(): T {
        if (value != null)
            return value!!.get()

        if (!isPresent())
            (fabricRegistryObject as RegistryObjectInjection).updateRef()

        if (!isPresent())
            println("still missing. debug please go back in time for this one.")

        return fabricRegistryObject.get()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RegistryObject<*>)
            return false

        return fabricRegistryObject == other.fabricRegistryObject
    }

    fun filter(predicate: Predicate<in T>): RegistryObject<T> {
        return RegistryObject(fabricRegistryObject.filter(predicate))
    }

    fun <U> flatMap(mapper: java.util.function.Function<in T, Optional<U>>): Optional<U> {
        return fabricRegistryObject.flatMap(mapper)
    }

    val holder: Optional<Holder<T>>
        get() {
            return fabricRegistryObject.holder
        }

    val id: ResourceLocation
        get() {
            return fabricRegistryObject.id
        }

    val key: ResourceKey<T>?
        get() {
            return fabricRegistryObject.key
        }

    override fun hashCode(): Int {
        return fabricRegistryObject.hashCode()
    }

    fun ifPresent(consumer: Consumer<in T>) {
        return fabricRegistryObject.ifPresent(consumer)
    }

    fun isPresent(): Boolean {
        return fabricRegistryObject.isPresent
    }

    private val registry: Registry<T>?
        get() {
            return Registry.REGISTRY.get(key!!.registry()) as Registry<T>?
                ?: return BuiltinRegistries.REGISTRY.get(key!!.registry()) as Registry<T>?
        }

    fun <U> lazyMap(mapper: java.util.function.Function<in T, out U>): Supplier<U> {
        return fabricRegistryObject.lazyMap(mapper)
    }

    fun <U> map(mapper: java.util.function.Function<in T, out U>): Optional<U> {
        return fabricRegistryObject.map(mapper)
    }

    fun orElse(other: T): T {
        return fabricRegistryObject.orElse(other)
    }

    fun orElseGet(other: Supplier<out T>): T {
        return fabricRegistryObject.orElseGet(other)
    }

    fun <X : Throwable> orElseThrow(exceptionSupplier: Supplier<out X>): T {
        return fabricRegistryObject.orElseThrow(exceptionSupplier)
    }

    fun stream(): Stream<T> {
        return fabricRegistryObject.stream()
    }

    companion object {
        @JvmStatic
        fun <T, U : T> create(name: ResourceLocation, registry: IForgeRegistry<T>): RegistryObject<U> {
            return RegistryObject(FabricRegistryObject(name, registry.registryKey))
        }

        @JvmStatic
        fun <T, U : T> create(name: ResourceLocation, registryKey: ResourceKey<out Registry<T>>, modid: String): RegistryObject<U> {
            return RegistryObject(FabricRegistryObject(name, registryKey), modid)
        }

        @JvmStatic
        fun <T, U : T> createOptional(name: ResourceLocation, registry: IForgeRegistry<T>): RegistryObject<U> {
            return RegistryObject(FabricRegistryObject(name, registry.registryKey))
        }

        @JvmStatic
        fun <T, U : T> createOptional(name: ResourceLocation, registryKey: ResourceKey<out Registry<T>>, modid: String): RegistryObject<U> {
            return RegistryObject(FabricRegistryObject(name, registryKey), modid)
        }
    }
}