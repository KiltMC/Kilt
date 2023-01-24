package xyz.bluspring.kilt.remaps.math

import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.world.phys.Vec3

class Vector3fRemap : Vector3f {
    constructor() : super()
    constructor(x: Float, y: Float, z: Float) : super(x, y, z)
    constructor(vector4f: Vector4f) : super(vector4f)
    constructor(vec3: Vec3) : super(vec3)

    companion object {
        @JvmStatic
        fun of(floats: FloatArray): Vector3f {
            return Vector3f(floats[0], floats[1], floats[2])
        }
    }
}