package xyz.bluspring.kilt.util

import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import net.neoforged.neoforgespi.language.IModInfo
import xyz.bluspring.kilt.loader.mod.NeoForgeMod

@Suppress("UnstableApiUsage")
fun Collection<NeoForgeMod>.buildGraph(): MutableGraph<NeoForgeMod> {
    val graph = GraphBuilder.directed().build<NeoForgeMod>()

    for (mod in this) {
        graph.addNode(mod)
    }

    for (mod in this) {
        for (dep in mod.dependencies) {
            val associatedMod = firstOrNull { it.modId == dep.modId } ?: continue

            if (associatedMod == mod)
                continue

            if (dep.ordering == IModInfo.Ordering.BEFORE) {
                graph.putEdge(mod, associatedMod)
            } else if (dep.ordering == IModInfo.Ordering.AFTER) {
                graph.putEdge(associatedMod, mod)
            }
        }
    }
    return graph
}