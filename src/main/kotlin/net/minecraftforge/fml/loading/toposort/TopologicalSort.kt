package net.minecraftforge.fml.loading.toposort

import com.google.common.graph.Graph
import java.util.*


object TopologicalSort {
    @JvmStatic
    fun <T> topologicalSort(graph: Graph<T>, comparator: Comparator<in T>?): List<T> {
        return io.github.fabricators_of_create.porting_lib.util.TopologicalSort.topologicalSort(graph, comparator)
    }
}