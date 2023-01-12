package net.minecraftforge.event

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.ReloadableServerResources
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.eventbus.api.Event
import xyz.bluspring.kilt.injections.ReloadableServerResourcesInjection
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class AddReloadListenerEvent(val serverResources: ReloadableServerResources) : Event() {
    val listeners = mutableListOf<PreparableReloadListener>()

    fun addListener(listener: PreparableReloadListener) {
        listeners.add(WrappedStateAwareListener(listener))
    }

    val conditionContext: ICondition.IContext
        get() = (serverResources as ReloadableServerResourcesInjection).conditionContext

    private class WrappedStateAwareListener(private val wrapped: PreparableReloadListener) : PreparableReloadListener {
        override fun reload(
            preparationBarrier: PreparableReloadListener.PreparationBarrier,
            resourceManager: ResourceManager,
            profilerFiller: ProfilerFiller,
            profilerFiller2: ProfilerFiller,
            executor: Executor,
            executor2: Executor
        ): CompletableFuture<Void> {
            return if (FabricLoader.getInstance().isModLoaded("kilt"))
                wrapped.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2)
            else
                CompletableFuture.completedFuture(null)
        }
    }
}