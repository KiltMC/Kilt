package xyz.bluspring.kilt.util

import fish.cichlidmc.tinycodecs.api.codec.Codec
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec
import java.util.Locale.getDefault

inline fun <reified T : Enum<T>> enumThrowingFallbackCodec(ignoreCase: Boolean = true): Codec<T> {
    return if (ignoreCase) {
        Codec.STRING.xmap(
            { enumValueOf<T>(it.uppercase()) },
            { it.name.lowercase(getDefault()) }
        )
    } else {
        Codec.STRING.xmap(
            { enumValueOf<T>(it) },
            { it.name }
        )
    }
}

inline fun <K : Any, V : Any> unboundedMap(keyCodec: Codec<K>, valueCodec: Codec<V>): Codec<Map<K, V>> {
    return MapCodec.map(keyCodec, valueCodec).asCodec()
}
