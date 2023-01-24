package xyz.bluspring.kilt.remaps.client.renderer.block.model

import com.mojang.math.Vector3f
import net.minecraft.client.renderer.block.model.ItemTransform

open class ItemTransformRemap(vector3f: Vector3f, vector3f2: Vector3f, vector3f3: Vector3f, rightRotation: Vector3f) : ItemTransform(vector3f, vector3f2, vector3f3) {
    @JvmField val rightRotation = rightRotation.copy()

    constructor(vector3f: Vector3f, vector3f2: Vector3f, vector3f3: Vector3f) : this(vector3f, vector3f2, vector3f3, Vector3f.ZERO)
}