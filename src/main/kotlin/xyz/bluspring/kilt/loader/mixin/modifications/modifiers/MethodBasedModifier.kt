package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

interface MethodBasedModifier : MixinModifier {
    val methods: List<String>
    var mappedMethods: List<String>

    override fun asString(): String {
        return "$this + (mappedOwner = $mappedOwner, mappedMethods = $mappedMethods)"
    }

    fun matches(methodParam: Any): Boolean {
        return this.methods.any { methodParam == it || (methodParam as List<String>).any { a -> a == it } }
                || this.mappedMethods.any { methodParam == it || (methodParam as List<String>).any { a -> a == it } }
    }
}