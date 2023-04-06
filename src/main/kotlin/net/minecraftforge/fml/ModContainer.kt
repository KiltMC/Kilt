package net.minecraftforge.fml

import net.minecraftforge.fml.config.IConfigEvent
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.forgespi.language.IModInfo
import xyz.bluspring.kilt.remaps.fml.config.ModConfigRemap
import java.util.EnumMap
import java.util.Optional
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

    // believe it or not, this is needed.
    // why? don't know, really.
    fun addConfig(modConfig: ModConfigRemap) {
        configs[modConfig.type] = modConfig
    }

    fun dispatchConfigEvent(event: IConfigEvent) {
        configHandler.ifPresent {
            it.accept(event)
        }
    }
}