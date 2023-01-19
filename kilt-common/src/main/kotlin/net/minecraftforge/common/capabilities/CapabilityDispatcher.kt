package net.minecraftforge.common.capabilities

import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import common.util.INBTSerializable
import common.util.LazyOptional

class CapabilityDispatcher(
    list: Map<ResourceLocation, common.capabilities.ICapabilityProvider>,
    private val listeners: List<Runnable>,
    parent: common.capabilities.ICapabilityProvider?
) : common.util.INBTSerializable<CompoundTag>,
    common.capabilities.ICapabilityProvider {
    private val capabilities: Array<common.capabilities.ICapabilityProvider>
    private val writers: Array<common.util.INBTSerializable<Tag>>
    private val names: Array<String>

    constructor(
        list: Map<ResourceLocation, common.capabilities.ICapabilityProvider>,
        listeners: List<Runnable>
    ) : this(list, listeners, null)

    init {
        val capsList = mutableListOf<common.capabilities.ICapabilityProvider>()
        val writersList = mutableListOf<common.util.INBTSerializable<Tag>>()
        val namesList = mutableListOf<String>()

        // Parents go first!
        // (i had to leave that comment)
        if (parent != null) {
            capsList.add(parent)
            if (parent is common.util.INBTSerializable<*>) {
                writersList.add(parent as common.util.INBTSerializable<Tag>)
                namesList.add("Parent")
            }
        }

        list.forEach { (location, provider) ->
            capsList.add(provider)

            if (provider is common.util.INBTSerializable<*>) {
                writersList.add(provider as common.util.INBTSerializable<Tag>)
                namesList.add(location.toString())
            }
        }

        capabilities = capsList.toTypedArray()
        writers = writersList.toTypedArray()
        names = namesList.toTypedArray()
    }

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): common.util.LazyOptional<T> {
        capabilities.forEach {
            val optional = it.getCapability(cap, side)

            if (optional.isPresent)
                return optional
        }

        return common.util.LazyOptional.empty()
    }

    override fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        writers.forEachIndexed { index, writer ->
            tag.put(names[index], writer.serializeNBT())
        }
        return tag
    }

    override fun deserializeNBT(nbt: CompoundTag?) {
        writers.forEachIndexed { index, writer ->
            if (nbt?.contains(names[index]) == true) {
                writer.deserializeNBT(nbt.get(names[index]))
            }
        }
    }

    fun areCompatible(other: CapabilityDispatcher?): Boolean {
        if (other == null)
            return writers.isEmpty()

        if (writers.isEmpty())
            return other.writers.isEmpty()

        return serializeNBT() == other.serializeNBT()
    }

    fun invalidate() {
        listeners.forEach {
            it.run()
        }
    }
}