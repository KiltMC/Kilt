package xyz.bluspring.knit.loader.fabric.mod.metadata

import net.fabricmc.loader.api.metadata.CustomValue

class CustomObjectValue(val map: MutableMap<String, CustomValue>) : CustomValue.CvObject {
    override fun size(): Int {
        return map.size
    }

    override fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun get(key: String?): CustomValue? {
        return map[key]
    }

    override fun iterator(): MutableIterator<Map.Entry<String, CustomValue>> {
        return map.iterator()
    }

    override fun getType(): CustomValue.CvType? {
        return CustomValue.CvType.OBJECT
    }

    override fun getAsObject(): CustomValue.CvObject {
        return this
    }

    override fun getAsArray(): CustomValue.CvArray? {
        TODO("Not yet implemented")
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