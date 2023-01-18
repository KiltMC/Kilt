package net.minecraftforge.fml

import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Consumer

interface IModLoadingState {
    fun name(): String
    fun previous(): String
    fun phase(): ModLoadingPhase
    fun message(): java.util.function.Function<ModList, String>
    fun inlineRunnable(): Optional<Consumer<ModList>>

    fun buildTransition(syncExecutor: Executor, parallelExecutor: Executor): Optional<CompletableFuture<Void>> {
        return Optional.empty()
    }

    fun buildTransition(syncExecutor: Executor, parallelExecutor: Executor,
                        preSyncTask: java.util.function.Function<Executor, CompletableFuture<Void>>,
                        postSyncTask: java.util.function.Function<Executor, CompletableFuture<Void>>
    ): Optional<CompletableFuture<Void>>
}