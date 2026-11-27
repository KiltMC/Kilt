package xyz.bluspring.kilt.loader.mixin.modifications.modifiers

interface MethodBasedModifier : MixinModifier {
    val methods: List<String>

    fun matches(methodParam: Any): Boolean {
        return this.methods.any { methodParam == it || (methodParam as List<String>).any { a -> a == it } }
    }
}
