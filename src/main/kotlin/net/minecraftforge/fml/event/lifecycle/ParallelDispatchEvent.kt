package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.fml.DeferredWorkQueue
import net.minecraftforge.fml.ModLoadingStage
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

// TODO: this should probably be made better
open class ParallelDispatchEvent(mod: ForgeMod?, private val stage: ModLoadingStage?) : ModLifecycleEvent(mod) {
    constructor() : this(null, null)

    private val queue = DeferredWorkQueue.lookup(Optional.ofNullable(stage))

    fun enqueueWork(work: Runnable): CompletableFuture<Void> {
        return queue.orElseThrow().enqueueWork(this.container!!, work)
    }

    fun <T> enqueueWork(work: Supplier<T>): CompletableFuture<T> {
        return queue.orElseThrow().enqueueWork(this.container!!, work)
    }
}