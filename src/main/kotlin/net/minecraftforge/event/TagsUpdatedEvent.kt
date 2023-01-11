package net.minecraftforge.event

import net.minecraft.core.RegistryAccess
import net.minecraftforge.eventbus.api.Event

class TagsUpdatedEvent(val registryAccess: RegistryAccess, fromClientPacket: Boolean, private val isIntegratedServerConnection: Boolean) : Event() {
    val updateCause = if (fromClientPacket) UpdateCause.CLIENT_PACKET_RECEIVED else UpdateCause.SERVER_DATA_LOAD

    fun shouldUpdateStaticData(): Boolean {
        return updateCause == UpdateCause.SERVER_DATA_LOAD || !isIntegratedServerConnection
    }

    enum class UpdateCause {
        SERVER_DATA_LOAD,
        CLIENT_PACKET_RECEIVED
    }
}