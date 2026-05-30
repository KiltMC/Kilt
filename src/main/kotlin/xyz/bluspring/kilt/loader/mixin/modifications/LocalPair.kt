package xyz.bluspring.kilt.loader.mixin.modifications

import com.llamalad7.mixinextras.sugar.Local
import org.objectweb.asm.Type

@JvmRecord
data class LocalPair(
    val descriptor: String,
    val local: Local,
    val unboxRef: Boolean = true
) {
    constructor(type: Type, local: Local) : this(type.descriptor, local)
}
