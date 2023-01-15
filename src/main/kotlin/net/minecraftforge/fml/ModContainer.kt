package net.minecraftforge.fml

import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.forgespi.language.IModInfo
import java.util.Optional
import java.util.function.Supplier

abstract class ModContainer(info: IModInfo) {
    val modId = info.modId
    val namespace = modId
    protected val modInfo = info
    protected var modLoadingStage = ModLoadingStage.CONSTRUCT
    protected var contextExtension: Supplier<*>? = null
    protected val activityMap = mutableMapOf<ModLoadingStage, Runnable>()
    protected val extensionPoints = mutableMapOf<Class<*>, Supplier<*>>()

    val currentStage: ModLoadingStage
        get() = modLoadingStage

    fun <T : Record> getCustomExtension(point: Class<out IExtensionPoint<T>>): Optional<T> {
        return Optional.ofNullable(extensionPoints.getOrDefault(point, Supplier { null }).get() as T)
    }

    fun <T> registerExtensionPoint(point: Class<out IExtensionPoint<T>>, extension: Supplier<T>) where T : Record, T : IExtensionPoint<T> {
        extensionPoints[point] = extension
    }
}