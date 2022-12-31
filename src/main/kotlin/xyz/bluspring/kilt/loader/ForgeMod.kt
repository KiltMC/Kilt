package xyz.bluspring.kilt.loader

import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.forgespi.language.IConfigurable
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.jar.JarFile

class ForgeMod(
    val modInfo: ForgeModInfo,
    val modFile: File,
    val modConfig: IConfigurable
) {
    val eventBus: IEventBus = BusBuilder.builder().apply {
        setExceptionHandler(this@ForgeMod::onEventFailed)
        setTrackPhases(false)
        markerType(IModBusEvent::class.java)
    }.build()

    val jar = JarFile(modFile)
    val secureJar: SecureJar = SecureJar.from(modFile.toPath())

    private fun onEventFailed(
        iEventBus: IEventBus,
        event: Event,
        iEventListeners: Array<IEventListener>,
        i: Int,
        throwable: Throwable
    ) {
        logger.error(EventBusErrorMessage(event, i, iEventListeners, throwable))
    }

    companion object {
        private val logger = LogManager.getLogger()
    }
}
