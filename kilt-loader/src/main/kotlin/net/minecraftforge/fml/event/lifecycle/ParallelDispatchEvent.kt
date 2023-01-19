package net.minecraftforge.fml.event.lifecycle

import net.minecraftforge.fml.ModLoadingStage
import xyz.bluspring.kilt.loader.ForgeMod
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

// TODO: this should probably be made better
open class ParallelDispatchEvent(mod: ForgeMod, private val stage: ModLoadingStage) : ModLifecycleEvent(mod) {
    fun enqueueWork(work: Runnable): CompletableFuture<Void> {
        return CompletableFuture.runAsync(work)
    }

    fun <T> enqueueWork(work: Supplier<T>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(work)
    }
}