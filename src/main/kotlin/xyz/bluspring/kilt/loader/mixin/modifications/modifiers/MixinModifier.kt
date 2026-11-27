package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

interface MixinModifier {
    val owner: String

    fun asString(): String {
        return "$this"
    }
}
