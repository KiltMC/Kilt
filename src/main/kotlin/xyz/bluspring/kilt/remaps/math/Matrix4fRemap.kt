package xyz.bluspring.kilt.remaps.math

import com.mojang.math.Matrix4f

object Matrix4fRemap {
    @JvmStatic
    fun of(values: FloatArray): Matrix4f {
        return Matrix4f().apply {
            this.fromFloatArray(values)
        }
    }
}