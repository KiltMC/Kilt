package net.minecraftforge.common.capabilities

import energy.IEnergyStorage
import fluids.capability.IFluidHandler
import fluids.capability.IFluidHandlerItem
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.items.IItemHandler
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

        if (!kiltHasRegistered)
            kiltRegisterCapabilities()

        autos.forEach {
            get<Any>(it.internalName, true)
        }

        val event = common.capabilities.RegisterCapabilitiesEvent()
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
        private var kiltHasRegistered = false

        private fun kiltRegisterCapabilities() {
            // Kilt doesn't scan itself for auto-registered capabilities, so just register them here
            // manually.
            INSTANCE.get<energy.IEnergyStorage>(Type.getInternalName(energy.IEnergyStorage::class.java), true)
            INSTANCE.get<fluids.capability.IFluidHandler>(Type.getInternalName(fluids.capability.IFluidHandler::class.java), true)
            INSTANCE.get<fluids.capability.IFluidHandlerItem>(Type.getInternalName(fluids.capability.IFluidHandlerItem::class.java), true)
            INSTANCE.get<net.minecraftforge.items.IItemHandler>(Type.getInternalName(
                net.minecraftforge.items.IItemHandler::class.java), true)

            kiltHasRegistered = true
        }

        @JvmStatic
        fun <T> get(type: CapabilityToken<T>): Capability<T> {
            return INSTANCE.get(type.getType(), false)
        }
    }
}