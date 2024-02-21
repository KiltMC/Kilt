package net.minecraftforge.registries

import com.google.common.base.Preconditions
import com.google.common.collect.*
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
import net.minecraft.world.entity.ai.village.poi.PoiType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.registries.IdMappingEvent.IdRemapping
import net.minecraftforge.registries.tags.ITagManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.mixin.MappedRegistryAccessor
import xyz.bluspring.kilt.mixin.PoiTypesAccessor
import java.util.*


open class FabricWrappedForgeRegistry<V : Any> constructor (
    private val stage: RegistryManager,
    override val kiltRegistryName: ResourceLocation,
    val builder: RegistryBuilder<V>
) : IForgeRegistryInternal<V>, IForgeRegistryModifiable<V> {
    val min = builder.minId
    val max = builder.maxId
    val create = builder.create
    val add: IForgeRegistry.AddCallback<V>?
    val clear = builder.clear
    val validate = builder.validate
    val bake = builder.bake
    val missing = builder.missingFactory
    val allowOverrides = builder.allowOverrides
    val isModifiable = builder.allowModifications
    val hasWrapper = builder.hasWrapper

    var isFrozen: Boolean = false

    init {
        if (this.create != null)
            this.create.onCreate(this@FabricWrappedForgeRegistry, stage)

        this.add = builder.add
    }

    override val kiltRegistryKey: ResourceKey<Registry<V>> = ResourceKey.createRegistryKey(kiltRegistryName)
    val key = kiltRegistryKey
    val fabricRegistry = LazyRegistrar.create<V>(kiltRegistryName, Kilt.MOD_ID)
    private val vanillaRegistryGetter = fabricRegistry.makeRegistry()
    val vanillaRegistry: Registry<V>
        get() {
            return vanillaRegistryGetter.get()
        }

    private val aliases = mutableMapOf<ResourceLocation, ResourceLocation>()

    private val tagManager = ForgeRegistryTagManager(this)

    override val kiltKeys: Set<ResourceLocation>
        get() {
            return vanillaRegistry.keySet()
        }
    override val kiltValues: Collection<V>
        get() {
            return vanillaRegistry.entrySet().map { it.value }
        }
    override val kiltEntries: Set<Map.Entry<ResourceKey<V>, V>>
        get() {
            return vanillaRegistry.entrySet()
        }
    override val kiltCodec: Codec<V>
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

    val owners = HashBiMap.create<OverrideOwner<V>, V>()
    val overrides = ArrayListMultimap.create<ResourceLocation, V>()

    override fun getKey(value: V): ResourceLocation? {
        val vanillaKey = vanillaRegistry.getKey(value)

        if (vanillaKey !== null)
            return vanillaKey

        val fabricKey = fabricRegistry.entries.firstOrNull { it.isPresent && it.get() == value }

        return fabricKey?.id
    }

    override fun containsValue(value: V): Boolean {
        return kiltValues.any { it == value }
    }

    override fun register(key: ResourceLocation, value: V) {
        this.add?.onAdd(this, stage, vanillaRegistry.size() + fabricRegistry.entries.size, ResourceKey.create(this.kiltRegistryKey, key), value, null)
        fabricRegistry.register(key) { value }
    }

    override fun register(key: String, value: V) {
        this.add?.onAdd(this, stage, vanillaRegistry.size() + fabricRegistry.entries.size, ResourceKey.create(this.kiltRegistryKey, ResourceLocation.tryParse(key)!!), value, null)
        fabricRegistry.register(key) { value }
    }

    override fun getHolder(location: ResourceLocation): Optional<Holder<V>> {
        return vanillaRegistry.getHolder(ResourceKey.create(kiltRegistryKey, location)) as Optional<Holder<V>>
    }

    override fun tags(): ITagManager<V> {
        return tagManager
    }

    override fun getDelegate(key: ResourceLocation): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.key().location().equals(key) })
    }

    override fun getDelegate(value: V): Optional<Holder.Reference<V>> {
        return Optional.ofNullable(vanillaRegistry.holders().toList().firstOrNull { it.value() == value })
    }

    override fun getDelegateOrThrow(key: ResourceLocation): Holder.Reference<V> {
        return vanillaRegistry.holders().toList().first { it.key().location().equals(key) }
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

        return vanillaRegistry.getHolder(key) as Optional<Holder<V>>
    }

    override fun getHolder(key: ResourceKey<V>): Optional<Holder<V>> {
        return vanillaRegistry.getHolder(key) as Optional<Holder<V>>
    }

    override fun iterator(): MutableIterator<V> {
        return vanillaRegistry.iterator()
    }

    override fun addAlias(src: ResourceLocation, dst: ResourceLocation) {
        @Suppress("ReplaceCallWithBinaryOperator") // aaaa
        if (src.equals(dst)) {
            return
        }

        this.aliases[src] = dst
    }

    override fun clear() {

    }

    override fun remove(key: ResourceLocation): V {
        return vanillaRegistry.get(key)!!
    }

    override fun isLocked(): Boolean {
        return false
    }

    fun getID(name: ResourceLocation): Int {
        return if (!isEmpty())
            vanillaRegistry.getId(getValue(name))
        else
            fabricRegistry.entries.indexOfFirst { it.id.equals(name) }
    }

    fun getID(value: V?): Int {
        return vanillaRegistry.getId(value)
    }

    override fun getValue(id: Int): V? {
        return vanillaRegistry.getHolder(id).get().value()
    }

    fun getRaw(key: ResourceLocation): V? {
        return vanillaRegistry.get(key)
    }

    companion object {
        // why is this needed
        @JvmField
        val REGISTRIES: Marker = MarkerManager.getMarker("REGISTRIES")
    }

    open class Snapshot {
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
            if ((binary as Any?) == null) {
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
                if ((nbt as Any?) == null)
                    return snapshot

                nbt!!.getList("ids", 10).forEach {
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
                if ((buff as Any?) == null)
                    return Snapshot()

                val snapshot = Snapshot()

                var length = buff!!.readVarInt()
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

    // This is for everything that uses the builder, essentially. Oh, and methods too.
    // Everything is public though, because Java-Kotlin doesn't seem to bode well.

    fun size(): Int {
        return fabricRegistry.entries.size
    }

    val wrapper: NamespacedWrapper<V>?
        get() {
            if (!builder.hasWrapper)
                return null

            return if ((kiltDefaultKey as Any?) != null)
                getSlaveMap(NamespacedDefaultedWrapper.Factory.ID, NamespacedDefaultedWrapper::class.java) as NamespacedDefaultedWrapper<V>?
            else
                getSlaveMap(NamespacedWrapper.Factory.ID, NamespacedWrapper::class.java) as NamespacedWrapper<V>?
        }

    @JvmName("getWrapperOrThrow")
    fun getWrapperOrThrow(): Registry<V> {
        return wrapper ?: throw IllegalStateException("Cannot query wrapper for non-wrapped forge registry!")
    }

    @JvmName("onBindTags")
    fun onBindTags(tags: Map<TagKey<V>, HolderSet.Named<V>>, defaultedTags: Set<TagKey<V>>) {
        tagManager.bind(tags, defaultedTags)
    }

    @JvmName("copy")
    fun copy(stage: RegistryManager): FabricWrappedForgeRegistry<V> {
        return FabricWrappedForgeRegistry(stage, kiltRegistryName, builder)
    }

    // i still do not know what the fuck this is for
    override val kiltDefaultKey = builder.default

    // i feel like this could have literally any other name, but oh well.
    // need to abide by Forge's rules.
    @get:JvmName("getSlaves")
    var slaves = mutableMapOf<ResourceLocation, Any?>()

    private class KiltPoiTypeMap : MutableMap<BlockState, PoiType> {
        override val entries: MutableSet<MutableMap.MutableEntry<BlockState, PoiType>>
            get() {
                val map = PoiTypesAccessor.getTypeByState()

                return map.entries.associate { it.key to it.value.value() }.toMutableMap().entries
            }
        override val keys: MutableSet<BlockState>
            get() = PoiTypesAccessor.getTypeByState().keys

        override val size: Int
            get() = PoiTypesAccessor.getTypeByState().size

        override val values: MutableCollection<PoiType>
            get() = PoiTypesAccessor.getTypeByState().values.map { it.value() }.toMutableList()

        override fun clear() {
            PoiTypesAccessor.getTypeByState().clear()
        }

        override fun isEmpty(): Boolean {
            return PoiTypesAccessor.getTypeByState().isEmpty()
        }

        override fun remove(key: BlockState): PoiType? {
            return PoiTypesAccessor.getTypeByState().remove(key)?.value()
        }

        override fun putAll(from: Map<out BlockState, PoiType>) {
            from.forEach { (key, value) ->
                PoiTypesAccessor.getTypeByState()[key] = Holder.direct(value)
            }
        }

        override fun put(key: BlockState, value: PoiType): PoiType? {
            val old = PoiTypesAccessor.getTypeByState()[key]
            PoiTypesAccessor.getTypeByState()[key] = Holder.direct(value)

            return old?.value()
        }

        override fun get(key: BlockState): PoiType? {
            return PoiTypesAccessor.getTypeByState()[key]?.value()
        }

        override fun containsValue(value: PoiType): Boolean {
            return PoiTypesAccessor.getTypeByState().any { it.value.value() == value }
        }

        override fun containsKey(key: BlockState): Boolean {
            return PoiTypesAccessor.getTypeByState().containsKey(key)
        }
    }

    override fun <T> getSlaveMap(slaveMapName: ResourceLocation, type: Class<T>): T? {
        return when (slaveMapName) {
            GameData.BLOCK_TO_ITEM -> {
                Item.BY_BLOCK as T
            }

            GameData.BLOCKSTATE_TO_POINT_OF_INTEREST_TYPE -> {
                KiltPoiTypeMap() as T
            }

            GameData.BLOCKSTATE_TO_ID -> {
                Block.BLOCK_STATE_REGISTRY as T
            }

            else -> {
                slaves[slaveMapName] as T?
            }
        }
    }

    override fun setSlaveMap(name: ResourceLocation, obj: Any?) {
        if (this.slaves == null) // https://akm-img-a-in.tosshub.com/indiatoday/images/story/201701/jackie-story_647_012517032327.jpg
            this.slaves = mutableMapOf()

        this.slaves[name] = obj
    }

    override fun register(id: Int, key: ResourceLocation, value: V) {
        add(id, key, value)
    }

    @JvmName("validateContent")
    fun validateContent(registryName: ResourceLocation) {
        // validate deez nuts
        // if this is required. oh fuckin' well.
    }

    @JvmName("freeze")
    fun freeze() {
        vanillaRegistry.freeze()
    }

    @JvmName("dump")
    fun dump(name: ResourceLocation) {
        // fuck that
    }

    fun bake() {
        this.bake?.onBake(this, stage)
    }

    @JvmName("unfreeze")
    fun unfreeze() {
        // it did not work, needed to AW it
        (vanillaRegistry as MappedRegistryAccessor).isFrozen = false
    }

    @JvmName("resetDelegates")
    fun resetDelegates() {
        if (!builder.hasWrapper)
            return


    }

    @JvmName("sync")
    fun sync(name: ResourceLocation, from: ForgeRegistry<V>) {
        // realistically, this shouldn't be needed, since it *is* relying on
        // the Vanilla registry system internally here.
        // but if it is actually needed, well, fuck, guess I've eaten my words.
        this.clear?.onClear(this, stage)
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
    fun processMissingEvent(name: ResourceLocation, pool: ForgeRegistry<V>, mappings: List<MissingMappingsEvent.Mapping<V>>, missing: Map<ResourceLocation, Int>, remaps: Map<ResourceLocation, IdMappingEvent.IdRemapping>, defaulted: kotlin.collections.Collection<ResourceLocation>, failed: kotlin.collections.Collection<ResourceLocation>, injectNetworkDummies: Boolean) {
        // so much info.... fuck that
    }

    @get:JvmName("getDefault")
    val default: V?
        get() {
            if ((kiltDefaultKey as Any?) == null)
                return null

            return vanillaRegistry.get(kiltDefaultKey)
        }

    @JvmName("isDummied")
    fun isDummied(key: ResourceLocation): Boolean {
        return false
    }

    @JvmName("add")
    fun add(id: Int, key: ResourceLocation, value: V): Int {
        return add(id, key, value, "kilt")
    }

    @JvmName("add")
    fun add(id: Int, key: ResourceLocation, value: V, owner: String): Int {
        fabricRegistry.register(key) { value }

        return getID(key).apply {
            this@FabricWrappedForgeRegistry.add?.onAdd(this@FabricWrappedForgeRegistry, stage, this, ResourceKey.create(this@FabricWrappedForgeRegistry.kiltRegistryKey, key), value, null)
        }
    }

    @get:JvmName("getResourceKeys")
    val resourceKeys: Set<ResourceKey<V>>
        get() = this.vanillaRegistry.registryKeySet()

    fun makeSnapshot(): Snapshot {
        val snapshot = Snapshot()
        fabricRegistry.entries.forEach {
            snapshot.ids[it.id] = getID(it.id)
        }

        return snapshot
    }

    fun getIDRaw(value: V): Int {
        return vanillaRegistry.getId(value)
    }

    fun getIDRaw(name: ResourceLocation): Int {
        return vanillaRegistry.getId(vanillaRegistry.get(name))
    }

    fun loadIds(
        ids: Map<ResourceLocation, Int>,
        overrides: Map<ResourceLocation, String>,
        missing: MutableMap<ResourceLocation, Int>,
        remapped: MutableMap<ResourceLocation, IdRemapping>,
        old: ForgeRegistry<V>,
        name: ResourceLocation?
    ) {
        val ovs: MutableMap<ResourceLocation, String> = Maps.newHashMap(overrides)
        for ((itemName, newId) in ids) {
            val currId: Int = old.getIDRaw(itemName)
            if (currId == -1) {
                missing[itemName] = newId
                continue  // no block/item -> nothing to add
            } else if (currId != newId) {
                remapped[itemName] = IdRemapping(currId, newId)
            }
            var obj = old.getRaw(itemName)
            Preconditions.checkState(
                obj != null,
                "objectKey has an ID but no object. Reflection/ASM hackery? Registry bug?"
            )
            val lst: MutableList<V?> = Lists.newArrayList(old.overrides.get(itemName))
            var primaryName: String? = null
            if (old.overrides.containsKey(itemName)) {
                if (!overrides.containsKey(itemName)) {
                    lst.add(obj)
                    obj = old.overrides.get(itemName).iterator()
                        .next() //Get the first one in the list, Which should be the first one registered
                    primaryName = old.owners.inverse()[obj]!!.owner
                } else primaryName = overrides[itemName]
            }
            for (value in lst) {
                val owner = old.owners.inverse()[value] ?: continue

                if (primaryName == owner.owner) continue
                val realId = add(newId, itemName, value!!, owner.owner)
            }

            val realId = add(newId, itemName, obj!!, primaryName ?: itemName.namespace)
            if (realId != newId) {}
            ovs.remove(itemName)
        }
        for ((itemName, owner) in ovs) {
            val current: String = this.owners.inverse()[getRaw(itemName)]!!.owner
            if (owner != current) {
                val _new = this.owners[OverrideOwner<V>(owner, ResourceKey.create(this.key, itemName))]
                if (_new == null) {
                    continue
                }

                val newId = this.getID(itemName)
                val realId = this.add(newId, itemName, _new, owner)
                if (newId != realId) {}
            }
        }
    }

    val overrideOwners: Map<ResourceLocation, String>
        get() {
            val ret = mutableMapOf<ResourceLocation, String>()

            for (key in this.overrides.keySet()) {
                val obj = vanillaRegistry.get(key)
                val owner = this.owners.inverse()[obj]

                ret[key] = owner!!.owner
            }

            return ret
        }

    fun block(id: Int) {

    }

    data class OverrideOwner<V>(val owner: String, val key: ResourceKey<V>)
}