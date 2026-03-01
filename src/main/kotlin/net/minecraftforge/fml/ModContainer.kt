package net.minecraftforge.fml

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.config.ConfigTracker
import net.minecraftforge.fml.config.IConfigEvent
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.forgespi.language.IModInfo
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

abstract class ModContainer(info: IModInfo) {
    val modId = info.modId
    val namespace = modId
    val modInfo = info
    @JvmField protected var modLoadingStage = ModLoadingStage.CONSTRUCT
    @JvmField protected var contextExtension: Supplier<*>? = null
    @JvmField protected val activityMap = mutableMapOf<ModLoadingStage, Runnable>()
    @JvmField protected val extensionPoints = mutableMapOf<Class<*>, Supplier<*>>()
    @JvmField protected val configs = EnumMap<ModConfig.Type, ModConfig>(ModConfig.Type::class.java)
    @JvmField protected var configHandler: Optional<Consumer<IConfigEvent>> = Optional.empty()

    val currentState: ModLoadingStage
        get() = modLoadingStage

    fun <T : Record> getCustomExtension(point: Class<out IExtensionPoint<T>>): Optional<T> {
        return Optional.ofNullable(extensionPoints.getOrDefault(point, Supplier { null }).get() as T?)
    }

    fun <T> registerExtensionPoint(point: Class<out IExtensionPoint<T>>, extension: Supplier<T>) where T : Record, T : IExtensionPoint<T> {
        extensionPoints[point] = extension
    }

    fun addConfig(modConfig: ModConfig) {
        configs[modConfig.type] = modConfig
    }

    open fun dispatchConfigEvent(event: IConfigEvent) {
        configHandler.ifPresent {
            it.accept(event)
        }
    }

    protected open fun <T> acceptEvent(e: T) where T : Event, T : IModBusEvent {}
}