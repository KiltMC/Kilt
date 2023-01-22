package net.minecraftforge.registries

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.mojang.serialization.Codec
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar
import io.netty.buffer.Unpooled
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraftforge.registries.tags.ITagManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.mixin.MappedRegistryAccessor
import java.util.*
import java.util.function.Supplier
import kotlin.Comparator

class ForgeRegistry<V> internal constructor (
    private val stage: RegistryManager,
    override val registryName: ResourceLocation,
    val builder: RegistryBuilder<V>
) : IForgeRegistryInternal<V> {
    override val registryKey: ResourceKey<Registry<V>> = ResourceKey.createRegistryKey(registryName)
    private val fabricRegistry = LazyRegistrar.create<V>(registryName, Kilt.MOD_ID)
    private val vanillaRegistryGetter = fabricRegistry.makeRegistry()
    val vanillaRegistry: Registry<V>
        get() {
            return vanillaRegistryGetter.get()
        }

    private val tagManager = ForgeRegistryTagManager(this)

    override val keys: Set<ResourceLocation>
        get() {
            return vanillaRegistry.keySet()
        }
    override val values: Collection<V>
        get() {
            return vanillaRegistry.entrySet().map { it.value }
        }
    override val entries: Set<Map.Entry<ResourceKey<V>, V>>
        get() {
            return vanillaRegistry.entrySet()
        }
    override val codec: Codec<V>
        get() {
            return vanillaRegistry.byNameCodec()
        }

    override fun containsKey(key: ResourceLocation): Boolean {
        return vanillaRegistry.containsKey(key)
    }

    override fun isEmpty(): Boolean {
        return vanillaRegistry.keySet().isEmpty()
    }

    override fun getValue(key: ResourceLocation): V? {
        return vanillaRegistry.get(key)
    }

    override fun getResourceKey(value: V): Optional<ResourceKey<V>> {
        return vanillaRegistry.getResourceKey(value)
    }

    override fun getKey(value: V): ResourceLocation? {
        return vanillaRegistry.getKey(value)
    }

    override fun containsValue(value: V): Boolean {
        return values.any { it == value }
    }

    override fun register(key: ResourceLocation, value: V) {
        fabricRegistry.register(key) { value }
    }

    override fun register(key: String, value: V) {
        fabricRegistry.register(key) { value }
    }

    override fun getHolder(location: ResourceLocation): Optional<Holder<V>> {
        return vanillaRegistry.getHolder(ResourceKey.create(registryKey, location))
    }

    override fun tags(): ITagManager<V> {
        return tagManager
    }

    override fun getDelegate(key: ResourceLocation): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.key().location() == key })
    }

    override fun getDelegate(value: V): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.value() == value })
    }

    override fun getDelegateOrThrow(key: ResourceLocation): Holder.Reference<V> {
        return vanillaRegistry.holders().toList().first { it.key().location() == key }
    }

    override fun getDelegateOrThrow(value: V): Holder.Reference<V> {
        return vanillaRegistry.holders().toList().first { it.value() == value }
    }

    override fun getDelegateOrThrow(rkey: ResourceKey<V>): Holder.Reference<V> {
        return vanillaRegistry.holders().toList().first { it.key() == rkey }
    }

    override fun getDelegate(rkey: ResourceKey<V>): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.key() == rkey })
    }

    override fun getHolder(value: V): Optional<Holder<V>> {
        val key = vanillaRegistry.holders().toList().firstOrNull { it.value() == value }?.key() ?: return Optional.empty()

        return vanillaRegistry.getHolder(key)
    }

    override fun getHolder(key: ResourceKey<V>): Optional<Holder<V>> {
        return vanillaRegistry.getHolder(key)
    }

    override fun iterator(): Iterator<V> {
        return vanillaRegistry.iterator()
    }

    fun getID(name: ResourceLocation): Int {
        return fabricRegistry.entries.indexOfFirst { it.id == name }
    }

    fun getID(value: V?): Int {
        return fabricRegistry.entries.indexOfFirst { it.get() == value }
    }

    fun getValue(id: Int): V? {
        return fabricRegistry.entries.elementAt(id).get()
    }

    fun getRaw(key: ResourceLocation): V? {
        return vanillaRegistry.get(key)
    }

    companion object {
        // why is this needed
        @JvmField
        val REGISTRIES: Marker = MarkerManager.getMarker("REGISTRIES")
    }

    class Snapshot {
        private val sorter = Comparator<ResourceLocation> { a, b ->
            a.compareNamespaced(b)
        }
        @JvmField val ids: MutableMap<ResourceLocation, Int> = Maps.newTreeMap(sorter)
        @JvmField val aliases: MutableMap<ResourceLocation, ResourceLocation> = Maps.newTreeMap(sorter)
        @JvmField val blocked: MutableSet<Int> = Sets.newTreeSet()
        @JvmField val dummied: MutableSet<ResourceLocation> = Sets.newTreeSet(sorter)
        @JvmField val overrides: MutableMap<ResourceLocation, String> = Maps.newTreeMap(sorter)
        private var binary: FriendlyByteBuf? = null

        fun write(): CompoundTag {
            val data = CompoundTag()

            val ids = ListTag()
            this.ids.entries.forEach { (key, value) ->
                val tag = CompoundTag()
                tag.putString("K", key.toString())
                tag.putInt("V", value)
                ids.add(tag)
            }
            data.put("ids", ids)

            val aliases = ListTag()
            this.aliases.entries.forEach { (key, value) ->
                val tag = CompoundTag()
                tag.putString("K", key.toString())
                tag.putString("V", value.toString())
                aliases.add(tag)
            }
            data.put("aliases", aliases)

            val overrides = ListTag()
            this.overrides.entries.forEach { (key, value) ->
                val tag = CompoundTag()
                tag.putString("K", key.toString())
                tag.putString("V", value)
                overrides.add(tag)
            }
            data.put("overrides", overrides)

            data.putIntArray("blocked", this.blocked.sorted())

            val dummied = ListTag()
            this.dummied.sorted().forEach {
                dummied.add(StringTag.valueOf(it.toString()))
            }
            data.put("dummied", dummied)

            return data
        }

        @Synchronized
        fun getPacketData(): FriendlyByteBuf {
            if (binary == null) {
                val packet = FriendlyByteBuf(Unpooled.buffer())

                packet.writeVarInt(ids.size)
                ids.forEach { (k, v) ->
                    packet.writeResourceLocation(k)
                    packet.writeInt(v)
                }

                packet.writeVarInt(aliases.size)
                aliases.forEach { (k, v) ->
                    packet.writeResourceLocation(k)
                    packet.writeResourceLocation(v)
                }

                packet.writeVarInt(overrides.size)
                overrides.forEach { (k, v) ->
                    packet.writeResourceLocation(k)
                    packet.writeUtf(v, 0x100)
                }

                packet.writeVarInt(blocked.size)
                blocked.forEach(packet::writeVarInt)

                packet.writeVarInt(dummied.size)
                dummied.forEach(packet::writeResourceLocation)

                binary = packet
            }

            return FriendlyByteBuf(binary!!.slice())
        }

        companion object {
            @JvmStatic
            fun read(nbt: CompoundTag?): Snapshot {
                val snapshot = Snapshot()
                if (nbt == null)
                    return snapshot

                nbt.getList("ids", 10).forEach {
                    val compound = it as CompoundTag
                    snapshot.ids[ResourceLocation(compound.getString("K"))] = compound.getInt("V")
                }

                nbt.getList("aliases", 10).forEach {
                    val compound = it as CompoundTag
                    snapshot.aliases[ResourceLocation(compound.getString("K"))] = ResourceLocation(compound.getString("V"))
                }

                nbt.getList("overrides", 10).forEach {
                    val compound = it as CompoundTag
                    snapshot.overrides[ResourceLocation(compound.getString("K"))] = compound.getString("V")
                }

                snapshot.blocked.addAll(nbt.getIntArray("blocked").toList())

                nbt.getList("dummied", 8).forEach {
                    snapshot.dummied.add(ResourceLocation((it as StringTag).asString))
                }

                return snapshot
            }

            @JvmStatic
            fun read(buff: FriendlyByteBuf?): Snapshot {
                if (buff == null)
                    return Snapshot()

                val snapshot = Snapshot()

                var length = buff.readVarInt()
                for (i in 0..length)
                    snapshot.ids[buff.readResourceLocation()] = buff.readVarInt()

                length = buff.readVarInt()
                for (i in 0..length)
                    snapshot.aliases[buff.readResourceLocation()] = buff.readResourceLocation()

                length = buff.readVarInt()
                for (i in 0..length)
                    snapshot.overrides[buff.readResourceLocation()] = buff.readUtf(0x100)

                length = buff.readVarInt()
                for (i in 0..length)
                    snapshot.blocked.add(buff.readVarInt())

                length = buff.readVarInt()
                for (i in 0..length)
                    snapshot.dummied.add(buff.readResourceLocation())

                return snapshot
            }
        }
    }

    // This is for everything that uses the builder, essentially. Oh, and internal methods too.
    // Everything is public though, because Java-Kotlin internal doesn't seem to bode well.

    fun size(): Int {
        return fabricRegistry.entries.size
    }

    internal val wrapper: Registry<V>?
        get() {
            if (!builder.hasWrapper)
                return null

            return if (defaultKey != null)
                getSlaveMap(NamespacedDefaultedWrapper.Factory.ID, NamespacedDefaultedWrapper::class.java) as NamespacedDefaultedWrapper<V>
            else
                getSlaveMap(NamespacedWrapper.Factory.ID, NamespacedWrapper::class.java) as NamespacedWrapper<V>
        }

    @JvmName("getWrapperOrThrow")
    internal fun getWrapperOrThrow(): Registry<V> {
        return wrapper ?: throw IllegalStateException("Cannot query wrapper for non-wrapped forge registry!")
    }

    internal val holderHelper: Optional<NamespacedHolderHelper<V>>
        get() {
            val wrapper = this.wrapper
            if (wrapper !is IHolderHelperHolder<*>)
                return Optional.empty()

            return Optional.of(wrapper.holderHelper as NamespacedHolderHelper<V>)
        }

    @JvmName("onBindTags")
    internal fun onBindTags(tags: Map<TagKey<V>, HolderSet.Named<V>>, defaultedTags: Set<TagKey<V>>) {
        tagManager.bind(tags, defaultedTags)
    }

    @JvmName("copy")
    internal fun copy(stage: RegistryManager): ForgeRegistry<V> {
        return ForgeRegistry(stage, registryName, builder)
    }

    // i still do not know what the fuck this is for
    override val defaultKey = builder.default

    // i feel like this could have literally any other name, but oh well.
    // need to abide by Forge's rules.
    @JvmField internal val slaves = mutableMapOf<ResourceLocation, Any>()

    override fun <T> getSlaveMap(slaveMapName: ResourceLocation, type: Class<T>): T {
        return slaves[slaveMapName] as T
    }

    override fun setSlaveMap(name: ResourceLocation, obj: Any) {
        slaves[name] = obj
    }

    @JvmName("validateContent")
    internal fun validateContent(registryName: ResourceLocation) {
        // validate deez nuts
        // if this is required. oh fuckin' well.
    }

    @JvmName("freeze")
    internal fun freeze() {
        vanillaRegistry.freeze()
    }

    @JvmName("dump")
    internal fun dump(name: ResourceLocation) {
        // fuck that
    }

    fun bake() {
        if (builder.bake != null)
            builder.bake!!.onBake(this, stage)
    }

    @JvmName("unfreeze")
    internal fun unfreeze() {
        // hope this works
        (vanillaRegistry as MappedRegistryAccessor).setFrozen(false)
    }

    @JvmName("resetDelegates")
    internal fun resetDelegates() {
        if (!builder.hasWrapper)
            return


    }

    @JvmName("sync")
    internal fun sync(name: ResourceLocation, from: ForgeRegistry<V>) {
        if (this == from)
            throw IllegalArgumentException("maybe i am you...")

        // realistically, this shouldn't be needed, since it *is* relying on
        // the Vanilla registry system internally here.
        // but if it is actually needed, well, fuck, guess I've eaten my words.
    }

    fun getMissingEvent(name: ResourceLocation, map: Map<ResourceLocation, Int>): MissingMappingsEvent {
        val list = mutableListOf<MissingMappingsEvent.Mapping<V>>()
        val pool = RegistryManager.ACTIVE.getRegistry<V>(name)
        map.forEach { (rl, id) ->
            list.add(MissingMappingsEvent.Mapping<V>(this, pool, rl, id))
        }

        return MissingMappingsEvent(ResourceKey.createRegistryKey<V>(name), this,
            list as Collection<MissingMappingsEvent.Mapping<*>>
        )
    }

    @JvmName("processMissingEvent")
    internal fun processMissingEvent(name: ResourceLocation, pool: ForgeRegistry<V>, mappings: List<MissingMappingsEvent.Mapping<V>>, missing: Map<ResourceLocation, Int>, remaps: Map<ResourceLocation, IdMappingEvent.IdRemapping>, defaulted: kotlin.collections.Collection<ResourceLocation>, failed: kotlin.collections.Collection<ResourceLocation>, injectNetworkDummies: Boolean) {
        // so much info.... fuck that
    }

    @get:JvmName("getDefault")
    internal val default: V?
        get() {
            if (defaultKey == null)
                return null

            return vanillaRegistry.get(defaultKey)
        }

    @JvmName("isDummied")
    internal fun isDummied(key: ResourceLocation): Boolean {
        return false
    }

    @JvmName("add")
    internal fun add(id: Int, key: ResourceLocation, value: V): Int {
        return add(id, key, value, "kilt")
    }

    @JvmName("add")
    internal fun add(id: Int, key: ResourceLocation, value: V, owner: String): Int {
        fabricRegistry.register(key, { value })

        return getID(key)
    }

    @get:JvmName("getResourceKeys")
    internal val resourceKeys: Set<ResourceKey<V>>
        get() = this.vanillaRegistry.registryKeySet()
}