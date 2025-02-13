package xyz.bluspring.kilt.util

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltFlags
import java.util.*

/**
 * A profiler within Kilt to figure out how much time each task takes.
 */
object DeltaTimeProfiler {
    private val taskTree = mutableMapOf<String, Long>()
    private val timeStack = Stack<Pair<String, Long>>()
    private val averageStore = mutableMapOf<String, MutableList<Long>>()

    fun push(name: String) {
        if (name.contains("/"))
            throw IllegalArgumentException("Name must not contain /!")

        timeStack.push(Pair(name, System.currentTimeMillis()))
    }

    fun pop() {
        if (timeStack.isEmpty())
            throw IllegalStateException("Time stack is empty!")

        val name = timeStack.joinToString("/") { it.first }
        taskTree[name] = System.currentTimeMillis() - timeStack.peek().second
        averageStore.computeIfAbsent(name) { mutableListOf() }.add(System.currentTimeMillis() - timeStack.peek().second)

        timeStack.pop()
    }

    fun popPush(name: String) {
        if (timeStack.isNotEmpty())
            pop()

        push(name)
    }

    fun popAll() {
        for (i in 0 until timeStack.size) {
            pop()
        }
    }

    fun dumpTree() {
        if (!timeStack.isEmpty())
            Kilt.logger.warn("Time stack is not empty! Will only print currently available stacks.")

        if (!FabricLoader.getInstance().isDevelopmentEnvironment && !KiltFlags.ENABLE_PROFILING)
            return

        Kilt.logger.info("Time deltas:")
        for ((taskName, deltaTime) in taskTree.filter { it.key.indexOf('/') == -1 }) {
            printTreeLine(taskName, deltaTime)
        }
    }

    private fun printTreeLine(taskName: String, deltaTime: Long) {
        val names = taskName.split("/")
        val name = names.last()
        val dashes = names.size * 3

        if (averageStore.containsKey(taskName) && averageStore[taskName]!!.size > 1) {
            val list = averageStore[taskName]!!
            val avg = list.average()
            val total = list.sum()
            Kilt.logger.info("${"-".repeat(dashes)} $name (last: $deltaTime ms, avg: $avg ms, total: $total)")

            Kilt.logger.info("${"=".repeat(dashes + 2)} Top 20: ${list.sortedByDescending { it }.take(20).joinToString(", ") { "$it ms" }}")
        } else {
            Kilt.logger.info("${"-".repeat(dashes)} $name ($deltaTime ms)")
        }

        for ((childName, childTime) in taskTree.filter { it.key.startsWith(taskName) && it.key.removePrefix(taskName).startsWith("/") && it.key.count { c -> c == '/' } == names.size }) {
            printTreeLine(childName, childTime)
        }
    }
}