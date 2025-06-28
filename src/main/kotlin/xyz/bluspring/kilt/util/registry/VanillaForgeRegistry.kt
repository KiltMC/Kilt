package xyz.bluspring.kilt.util.registry

import com.google.common.base.Preconditions
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.client.Minecraft
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.RegistryDataLoader
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn
import net.minecraftforge.registries.DataPackRegistriesHooks
import net.minecraftforge.registries.ForgeRegistry
import net.minecraftforge.registries.RegistryBuilder
import net.minecraftforge.registries.RegistryManager
import net.minecraftforge.server.ServerLifecycleHooks
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class VanillaForgeRegistry<V> : ForgeRegistry<V> {
    var vanillaRegistry: Registry<V?>? = null

    constructor(stage: RegistryManager?, name: ResourceLocation?, builder: RegistryBuilder<V?>) : super(stage, name, builder) {
        // Kilt: Try to get/create the Vanilla registry
        val vanillaRegistry: Optional<out Registry<*>?> = BuiltInRegistries.REGISTRY.getOptional(name)

        if (vanillaRegistry.isPresent) {
            this.vanillaRegistry = vanillaRegistry.orElseThrow() as Registry<V?>
        } else if ( // Kilt: Make sure we're not loading datapack registries, we have to add these directly via external means
            !DataPackRegistriesHooks.getSyncedCustomRegistries().contains(this.key) &&
            RegistryDataLoader.WORLDGEN_REGISTRIES.stream()
                .noneMatch { e: RegistryDataLoader.RegistryData<*>? -> e!!.key() == this.key }
        ) {
            val registryBuilder =
                if (this.defaultKey == null) FabricRegistryBuilder.createSimple<V?>(this.key) else FabricRegistryBuilder.createDefaulted<V?>(
                    this.key,
                    this.defaultKey
                )

            registryBuilder.attribute(RegistryAttribute.SYNCED)

            if (builder.saveToDisc) registryBuilder.attribute(RegistryAttribute.PERSISTED)

            if (builder.allowModifications || builder.allowOverrides) registryBuilder.attribute(
                RegistryAttribute.MODDED
            )

            this.vanillaRegistry = registryBuilder.buildAndRegister()
        }
    }

    private fun vanillaRegistry(): Registry<V?>? {
        if (this.vanillaRegistry == null) {
            val server = ServerLifecycleHooks.getCurrentServer()

            if (server != null) {
                this.vanillaRegistry = server.registryAccess().registryOrThrow<V?>(this.key)
            } else {
                // Kilt: pray that this works
                unsafeRunWhenOn(Dist.CLIENT, Supplier {
                    Runnable {
                        if (Minecraft.getInstance().level != null) this.vanillaRegistry =
                            Minecraft.getInstance().level!!.registryAccess().registryOrThrow<V?>(this.key)
                    }
                })
            }

            val registry = this.vanillaRegistry
            this.vanillaRegistry = null

            return registry
        }

        return this.vanillaRegistry
    }

    override fun iterator(): MutableIterator<V?> {
        return vanillaRegistry()?.iterator() ?: Collections.emptyIterator()
    }

    override fun containsKey(key: ResourceLocation): Boolean {
        return vanillaRegistry()?.containsKey(key) ?: false
    }

    override fun containsValue(value: V): Boolean {
        return vanillaRegistry()?.getKey(value) != null;
    }

    override fun isEmpty(): Boolean {
        return vanillaRegistry()?.keySet()?.isEmpty() ?: true
    }

    override fun size(): Int {
        return vanillaRegistry()?.size() ?: 0
    }

    override fun getValue(key: ResourceLocation?): V? {
        return vanillaRegistry()?.get(key)
    }

    override fun getKey(value: V?): ResourceLocation? {
        return vanillaRegistry()?.getKey(value)
    }

    override fun getResourceKey(value: V?): Optional<ResourceKey<V?>?> {
        return vanillaRegistry()!!.getResourceKey(value)
    }

    override fun getHolder(key: ResourceKey<V?>?): Optional<Holder<V?>?> {
        return vanillaRegistry()!!.getHolder(key).map<Holder<V?>?>(Function.identity<Holder.Reference<V?>>())
    }

    override fun getHolder(location: ResourceLocation): Optional<Holder<V?>?> {
        return vanillaRegistry()!!.getHolder(ResourceKey.create<V?>(this.key, location))
            .map<Holder<V?>?>(Function.identity<Holder.Reference<V?>>())
    }

    override fun getHolder(value: V): Optional<Holder<V?>?> {
        return vanillaRegistry()!!.getResourceKey(value).flatMap<Holder<V?>?>(vanillaRegistry!!::getHolder)
    }

    override fun getKeys(): Set<ResourceLocation?> {
        return vanillaRegistry()!!.keySet()
    }

    override fun getResourceKeys(): Set<ResourceKey<V?>?> {
        return vanillaRegistry()!!.registryKeySet()
    }

    override fun getValues(): Collection<V?> {
        if (vanillaRegistry() == null) {
            return Collections.emptySet()
        }
        return vanillaRegistry()!!.stream().toList();
    }

    override fun getEntries(): Set<Map.Entry<ResourceKey<V?>?, V?>?> {
        return vanillaRegistry()!!.entrySet()
    }

    override fun getID(value: V?): Int {
        return vanillaRegistry()?.getId(value) ?: -1
    }

    override fun getID(name: ResourceLocation): Int {
        return vanillaRegistry()!!.getId(vanillaRegistry()!!.get(name));
    }

    override fun getIDRaw(value: V?): Int {
        return vanillaRegistry()!!.getId(value);
    }

    override fun getIDRaw(name: ResourceLocation?): Int {
        return vanillaRegistry()!!.getId(vanillaRegistry()!!.get(name))
    }

    override fun getValue(id: Int): V? {
        return vanillaRegistry()!!.byId(id)
    }

    override fun getKey(id: Int): ResourceKey<V?>? {
        val value: V? = vanillaRegistry()!!.byId(id)

        if (value == null) return null

        return vanillaRegistry()!!.getResourceKey(value).orElse(null)
    }

    override fun add(id: Int, key: ResourceLocation, value: V, owner: String?): Int {
        Preconditions.checkNotNull<ResourceLocation?>(key, "Can't use a null-name for the registry, object %s.", value)
        Preconditions.checkNotNull<V?>(value, "Can't add null-object to the registry, name %s.", key)

        val registry = vanillaRegistry()!!

        val currentId = if (registry.containsKey(key) && registry is MappedRegistry) {
            val id = registry.getId(registry.get(key))
            registry.registerMapping(id, ResourceKey.create(registry.key(), key), value, registry.registryLifecycle())
            id
        } else {
            registry.getId(Registry.register(registry, key, value))
        }

        this.add?.onAdd(this, this.stage, currentId, registry.getResourceKey(value).orElseThrow(), value, null)

        return currentId
    }

    override fun getRaw(key: ResourceLocation?): V? {
        return vanillaRegistry()!!.get(key)
    }

    override fun getDelegate(rkey: ResourceKey<V?>): Optional<Holder.Reference<V?>?> {
        return vanillaRegistry()!!.getHolder(rkey)
    }

    override fun getDelegateOrThrow(rkey: ResourceKey<V?>): Holder.Reference<V?> {
        return vanillaRegistry()!!.getHolderOrThrow(rkey)
    }

    override fun getDelegate(key: ResourceLocation): Optional<Holder.Reference<V?>?> {
        return vanillaRegistry()!!.getHolder(ResourceKey.create<V?>(vanillaRegistry()!!.key(), key))
    }

    override fun getDelegateOrThrow(key: ResourceLocation): Holder.Reference<V?> {
        return vanillaRegistry()!!.getHolderOrThrow(ResourceKey.create<V?>(vanillaRegistry()!!.key(), key))
    }

    override fun getDelegate(value: V): Optional<Holder.Reference<V?>> {
        return vanillaRegistry()!!.getResourceKey(value).flatMap<Holder.Reference<V?>>(vanillaRegistry!!::getHolder)
    }

    override fun getDelegateOrThrow(value: V): Holder.Reference<V?> {
        return vanillaRegistry()!!.getHolderOrThrow(vanillaRegistry()!!.getResourceKey(value).orElseThrow())
    }

    override fun validateContent(registryName: ResourceLocation?) {
        // Kilt: if this is a datapack registry, don't even bother
        if (this.vanillaRegistry == null)
            return
        super.validateContent(registryName)
    }

    override fun dump(name: ResourceLocation?) {}
}