package xyz.bluspring.kilt.loader.mixin.modifications

import org.objectweb.asm.Type

data class ParamPair(
    val descriptor: String,
    val ordinal: Int
) {
    constructor(type: Type, ordinal: Int) : this(type.descriptor, ordinal)
}
