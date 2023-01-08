package xyz.bluspring.kilt.loader

import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.eventbus.api.IEventListener
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.forgespi.language.IConfigurable
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import xyz.bluspring.kilt.Kilt
import java.io.File
import java.util.Optional
import java.util.function.Supplier
import java.util.jar.JarFile

class ForgeMod(
    val modInfo: ForgeModInfo,
    val modFile: File,
    val modConfig: IConfigurable
) {
    val eventBus: IEventBus = KiltLoader.modEventBus

    val jar: JarFile
        get() {
            return JarFile(remappedModFile)
        }
    lateinit var remappedModFile: File
    lateinit var scanData: ModFileScanData

    fun getSecureJar(): Supplier<SecureJar> {
        return Supplier {
            SecureJar.from(modFile.toPath())
        }
    }
}
