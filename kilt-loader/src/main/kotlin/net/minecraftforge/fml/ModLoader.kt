package net.minecraftforge.fml

import net.fabricmc.loader.impl.FabricLoaderImpl
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.locating.ForgeFeature
import xyz.bluspring.kilt.Kilt
import java.util.concurrent.Executor
import java.util.function.BiConsumer

class ModLoader {
    val warnings = mutableListOf<ModLoadingWarning>()

    fun addWarning(warning: ModLoadingWarning) {
        warnings.add(warning)
    }

    fun <T> postEvent(e: T) where T : Event, T : IModBusEvent {
        Kilt.loader.mods.forEach {
            it.eventBus.post(e)
        }
    }

    fun <T> runEventGenerator(generator: java.util.function.Function<ModContainer, T>) where T : Event, T : IModBusEvent {
        Kilt.loader.mods.forEach {
            it.eventBus.post(generator.apply(it.container))
        }
    }

    fun <T> postEventWithWrapInModOrder(e: T, pre: BiConsumer<ModContainer, T>, post: BiConsumer<ModContainer, T>) where T : Event, T : IModBusEvent {
        Kilt.loader.mods.forEach {
            pre.accept(it.container, e)
            it.eventBus.post(e)
            post.accept(it.container, e)
        }
    }

    // TODO: these aren't used by Kilt, so no point really. these are only added to appease the Forge API's compile errors.
    // right now it seems to be used by the ClientModLoader class, which appears to just load datapacks and resource packs and such?
    // maybe it can be updated to use Kilt internals later.
    fun gatherAndInitializeMods(syncExecutor: ModWorkManager.DrivenExecutor, parallelExecutor: Executor, periodicTask: Runnable) {
        ForgeFeature.registerFeature("java_version", ForgeFeature.VersionFeatureTest.forVersionString(IModInfo.DependencySide.SERVER, System.getProperty("java.version")))
    }

    fun loadMods(syncExecutor: ModWorkManager.DrivenExecutor, parallelExecutor: Executor, periodicTask: Runnable) {}
    fun finishMods(syncExecutor: ModWorkManager.DrivenExecutor, parallelExecutor: Executor, periodicTask: Runnable) {}

    companion object {
        private lateinit var instance: ModLoader

        @JvmStatic
        fun get(): ModLoader {
            return if (this::instance.isInitialized)
                ModLoader().apply {
                    instance = this
                }
            else instance
        }

        @JvmStatic
        fun isLoadingStateValid(): Boolean {
            return FabricLoaderImpl.INSTANCE.gameProvider.isEnabled
        }
    }
}