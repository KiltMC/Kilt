package cpw.mods.modlauncher

import cpw.mods.modlauncher.api.ITransformer
import java.util.concurrent.ConcurrentHashMap

class TransformList<T> internal constructor(private val nodeType: Class<T>) {
    private val transformers: MutableMap<TransformTargetLabel, MutableList<ITransformer<T>>> = ConcurrentHashMap()

    internal fun addTransformer(targetLabel: TransformTargetLabel, transformer: ITransformer<T>) {
        transformers.computeIfAbsent(targetLabel) { mutableListOf() }
        transformers.computeIfPresent(targetLabel) { k, l -> l.add(transformer); l }
    }
}
