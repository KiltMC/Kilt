package xyz.bluspring.kilt.loader.mixin.modifications

import com.llamalad7.mixinextras.sugar.Local
import org.objectweb.asm.Type

data class LocalPair(
    val descriptor: String,
    val local: Local
) {
    constructor(type: Type, local: Local) : this(type.descriptor, local)
}
