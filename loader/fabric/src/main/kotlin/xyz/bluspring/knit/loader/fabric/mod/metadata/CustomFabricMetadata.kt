package xyz.bluspring.knit.loader.fabric.mod.metadata

import net.fabricmc.loader.api.metadata.CustomValue

fun Any?.toMetadataValue(): CustomValue {
    return when (this) {
        is Number -> CustomNumberValue(this)
        is String -> CustomStringValue(this)
        is Boolean -> CustomBooleanValue(this)
        null -> CustomNullValue()
        is List<*> -> {
            val list = mutableListOf<CustomValue>()

            for (value in this) {
                list.add(value.toMetadataValue())
            }

            CustomArrayValue(list)
        }
        is Map<*, *> -> {
            val map = mutableMapOf<String, CustomValue>()

            for ((key, value) in this) {
                if (key !is String)
                    throw IllegalArgumentException("Invalid key $key in map for custom metadata!")

                map[key] = value.toMetadataValue()
            }

            CustomObjectValue(map)
        }

        else -> throw IllegalArgumentException("Invalid type for custom metadata! (got ${this.javaClass.simpleName})")
    }
}