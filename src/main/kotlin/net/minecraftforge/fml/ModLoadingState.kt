package net.minecraftforge.fml

import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Consumer
import java.util.function.Function

data class ModLoadingState(
    val name: String, val previous: String,
    val message: Function<ModList, String>,
    val phase: ModLoadingPhase,
    val inlineRunnable: Optional<Consumer<ModList>>,
    val transition: Optional<IModStateTransition>
) : IModLoadingState {
    override fun name(): String {
        return name
    }

    override fun previous(): String {
        return previous
    }

    override fun phase(): ModLoadingPhase {
        return phase
    }

    override fun message(): Function<ModList, String> {
        return message
    }

    override fun inlineRunnable(): Optional<Consumer<ModList>> {
        return inlineRunnable
    }

    override fun buildTransition(
        syncExecutor: Executor,
        parallelExecutor: Executor,
        preSyncTask: Function<Executor, CompletableFuture<Void>>,
        postSyncTask: Function<Executor, CompletableFuture<Void>>
    ): Optional<CompletableFuture<Void>> {
        // we don't have transitions in Kilt
        // don't bother
        // unless for some reason it's needed.
        // please don't tell me it's needed.
        return Optional.empty()
    }

    companion object {
        @JvmStatic
        fun withInline(name: String, previous: String, phase: ModLoadingPhase, inline: Consumer<ModList>): ModLoadingState {
            return ModLoadingState(name, previous, {
                "Processing work $name"
            }, phase, Optional.of(inline), Optional.empty())
        }
    }
}
