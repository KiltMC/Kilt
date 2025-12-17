package xyz.bluspring.kilt.loader.remap

@JvmRecord
data class MixinRefmap(
    val mappings: MutableMap<String, MutableMap<String, String>>,
    val alreadyRefmapped: MutableMap<String, MutableSet<String>>
)
