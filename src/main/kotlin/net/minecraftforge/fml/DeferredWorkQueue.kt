package net.minecraftforge.fml

import xyz.bluspring.kilt.Kilt
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Supplier

class DeferredWorkQueue(private val stage: ModLoadingStage) {
    private val tasks = ConcurrentLinkedQueue<Task>()

    init {
        queues[stage] = this
    }

    fun runTasks() {
        if (tasks.isEmpty())
            return

        val exception = RuntimeException()

        for (task in tasks) {
            ModLoadingContext.kiltActiveModId = task.owner.modId

            try {
                task.future.exceptionally {
                    Kilt.logger.error("Mod \"${task.owner.modId}\" encountered an error in a deferred task!")
                    it.printStackTrace()

                    exception.addSuppressed(it)
                    null
                }
                task.task.run()
            } finally {
                ModLoadingContext.kiltActiveModId = null
            }
        }

        if (exception.suppressed.isNotEmpty()) {
            throw exception
        }
    }

    fun enqueueWork(modInfo: ModContainer, work: Runnable): CompletableFuture<Void> {
        return addTask(modInfo) { task -> CompletableFuture.runAsync(work) { task.task = it } }
    }

    fun <T> enqueueWork(modInfo: ModContainer, work: Supplier<T>): CompletableFuture<T> {
        return addTask(modInfo) { task -> CompletableFuture.supplyAsync(work) { task.task = it } }
    }

    private fun <T> addTask(modInfo: ModContainer, futureSupplier: (Task) -> CompletableFuture<T>): CompletableFuture<T> {
        val task = Task(modInfo)
        val future = futureSupplier.invoke(task)
        task.future = future
        tasks.add(task)

        return future
    }

    companion object {
        private val queues = mutableMapOf<ModLoadingStage, DeferredWorkQueue>()

        @JvmStatic
        fun lookup(parallelClass: Optional<ModLoadingStage>): Optional<DeferredWorkQueue> {
            return Optional.ofNullable(queues[parallelClass.orElse(null)])
        }
    }

    private data class Task(
        val owner: ModContainer
    ) {
        lateinit var task: Runnable
        lateinit var future: CompletableFuture<*>
    }
}