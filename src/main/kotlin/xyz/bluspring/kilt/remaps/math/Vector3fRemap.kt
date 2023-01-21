package xyz.bluspring.kilt.remaps.math

import com.mojang.math.Vector3f

object Vector3fRemap {
    @JvmStatic
    fun of(floats: FloatArray): Vector3f {
        return Vector3f(floats[0], floats[1], floats[2])
    }
}