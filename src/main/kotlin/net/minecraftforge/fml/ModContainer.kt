package net.minecraftforge.fml

import net.minecraftforge.forgespi.language.IModInfo
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
}