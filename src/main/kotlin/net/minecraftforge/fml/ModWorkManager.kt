package net.minecraftforge.fml

import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.LockSupport

object ModWorkManager {
    private val PARK_TIME = TimeUnit.MILLISECONDS.toNanos(1L)

    interface DrivenExecutor : Executor {
        fun selfDriven(): Boolean
        fun driveOne(): Boolean

        fun drive(ticker: Runnable) {
            if (!selfDriven())
                while (driveOne())
                    ticker.run()
            else
                LockSupport.parkNanos(PARK_TIME)
        }
    }

    private class SyncExecutor : DrivenExecutor {
        private val tasks = ConcurrentLinkedDeque<Runnable>()

        override fun selfDriven(): Boolean {
            return false
        }

        override fun driveOne(): Boolean {
            val task = tasks.pollFirst()
            task?.run()

            return task != null
        }

        override fun execute(p0: Runnable) {
            tasks.addLast(p0)
        }
    }

    private class WrappingExecutor(private val wrapped: Executor) : DrivenExecutor {
        override fun selfDriven(): Boolean {
            return true
        }

        override fun driveOne(): Boolean {
            return false
        }

        override fun execute(p0: Runnable) {
            wrapped.execute(p0)
        }
    }

    private lateinit var syncExec: SyncExecutor

    @JvmStatic
    fun syncExecutor(): DrivenExecutor {
        if (!this::syncExec.isInitialized)
            syncExec = SyncExecutor()

        return syncExec
    }

    @JvmStatic
    fun wrappedExecutor(executor: Executor): DrivenExecutor {
        return WrappingExecutor(executor)
    }

    private lateinit var threadPool: ForkJoinPool
    @JvmStatic
    fun parallelExecutor(): Executor {
        if (!this::threadPool.isInitialized) {
            // TODO: Add a config for setting this.
            val loadingThreadCount = Runtime.getRuntime().availableProcessors()

            threadPool = ForkJoinPool(loadingThreadCount, ::newForkJoinWorkerThread, null, false)
        }

        return threadPool
    }

    private fun newForkJoinWorkerThread(pool: ForkJoinPool): ForkJoinWorkerThread {
        val thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
        thread.name = "kilt-modloading-worker-${thread.poolIndex}"
        thread.contextClassLoader = Thread.currentThread().contextClassLoader

        return thread
    }
}