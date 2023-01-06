package net.minecraftforge.common.capabilities

import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Type
import xyz.bluspring.kilt.Kilt
import java.util.IdentityHashMap

enum class CapabilityManager {
    INSTANCE;

    private val providers = IdentityHashMap<String, Capability<*>>()

    fun injectCapabilities(data: List<ModFileScanData>) {
        val autos = data
            .flatMap { it.annotations }
            .filter { it.annotationType == AUTO_REGISTER }
            .map { it.clazz }
            .distinct()
            .sortedBy { it.toString() }

        autos.forEach {
            get<Any>(it.internalName, true)
        }

        val event = RegisterCapabilitiesEvent()
        Kilt.loader.postEvent(event)
    }

    internal fun <T> get(realName: String, registering: Boolean): Capability<T> {
        var name = realName
        var cap: Capability<T>

        synchronized(providers) {
            // there's a fucking string intern?
            name = name.intern()
            cap = providers.computeIfAbsent(name) {
                return@computeIfAbsent Capability<T>(it)
            } as Capability<T> // why
        }

        if (registering) {
            synchronized(cap) {
                if (cap.isRegistered())
                    throw IllegalArgumentException("Cannot register a capability implementation multiple times : $name")

                cap.onRegister()
            }
        }

        return cap
    }

    companion object {
        private val AUTO_REGISTER = Type.getType(AutoRegisterCapability::class.java)
        private val logger = LogManager.getLogger()

        @JvmStatic
        fun <T> get(type: CapabilityToken<T>): Capability<T> {
            return INSTANCE.get(type.getType(), false)
        }
    }
}