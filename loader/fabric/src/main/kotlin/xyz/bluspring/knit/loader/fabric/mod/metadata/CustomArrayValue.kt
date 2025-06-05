package xyz.bluspring.knit.loader.fabric.mod.metadata

import net.fabricmc.loader.api.metadata.CustomValue

class CustomArrayValue(private val items: MutableList<CustomValue>) : CustomValue.CvArray {
    override fun size(): Int {
        return items.size
    }

    override fun get(index: Int): CustomValue? {
        return items[index]
    }

    override fun iterator(): MutableIterator<CustomValue> {
        return items.iterator()
    }

    override fun getType(): CustomValue.CvType {
        return CustomValue.CvType.ARRAY
    }

    override fun getAsObject(): CustomValue.CvObject? {
        TODO("Not yet implemented")
    }

    override fun getAsArray(): CustomValue.CvArray {
        return this
    }

    override fun getAsString(): String? {
        TODO("Not yet implemented")
    }

    override fun getAsNumber(): Number? {
        TODO("Not yet implemented")
    }

    override fun getAsBoolean(): Boolean {
        TODO("Not yet implemented")
    }
}