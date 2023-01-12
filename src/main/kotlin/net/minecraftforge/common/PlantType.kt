package net.minecraftforge.common

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

class PlantType private constructor(val name: String) {
    companion object {
        private val INVALID_CHARACTERS = Pattern.compile("[^a-z_]")
        private val values = ConcurrentHashMap<String, PlantType>()

        @JvmStatic
        fun get(name: String): PlantType {
            return values.computeIfAbsent(name) {
                if (INVALID_CHARACTERS.matcher(it).find())
                    throw IllegalArgumentException("PlantType.get() called with invalid name: $name")

                PlantType(it)
            }
        }

        @JvmField
        val PLAINS = get("plains")
        @JvmField
        val DESERT = get("desert")
        @JvmField
        val BEACH = get("beach")
        @JvmField
        val CAVE = get("cave")
        @JvmField
        val WATER = get("water")
        @JvmField
        val NETHER = get("nether")
        @JvmField
        val CROP = get("crop")
    }
}