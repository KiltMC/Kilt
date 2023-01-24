package xyz.bluspring.kilt.remaps.math

import com.mojang.math.Matrix4f
import com.mojang.math.Quaternion

class Matrix4fRemap : Matrix4f {
    constructor() : super()
    constructor(matrix4f: Matrix4f) : super(matrix4f)
    constructor(quaternion: Quaternion) : super(quaternion)

    companion object {
        @JvmStatic
        fun of(values: FloatArray): Matrix4f {
            return Matrix4f().apply {
                this.fromFloatArray(values)
            }
        }
    }
}