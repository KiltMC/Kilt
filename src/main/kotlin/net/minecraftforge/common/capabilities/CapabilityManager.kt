package net.minecraftforge.common.capabilities

import net.minecraftforge.energy.IEnergyStorage
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandlerItem
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
        private var kiltHasRegistered = false

        private fun kiltRegisterCapabilities() {
            // Kilt doesn't scan itself for auto-registered capabilities, so just register them here
            // manually.
            INSTANCE.get<IEnergyStorage>(Type.getInternalName(IEnergyStorage::class.java), true)
            INSTANCE.get<IFluidHandler>(Type.getInternalName(IFluidHandler::class.java), true)
            INSTANCE.get<IFluidHandlerItem>(Type.getInternalName(IFluidHandlerItem::class.java), true)
            INSTANCE.get<IItemHandler>(Type.getInternalName(IItemHandler::class.java), true)

            kiltHasRegistered = true
        }

        @JvmStatic
        fun <T> get(type: CapabilityToken<T>): Capability<T> {
            return INSTANCE.get(type.getType(), false)
        }
    }
}