package net.minecraftforge.network

import net.fabricmc.api.EnvType
import java.util.function.BiFunction
import java.util.function.Supplier

enum class NetworkDirection(
    val eventSupplier: BiFunction<ICustomPacket<*>, Supplier<NetworkEvent.Context>, NetworkEvent>,
    val side: EnvType,
    private val response: Int
) {
    PLAY_TO_SERVER(NetworkEvent::ClientCustomPayloadEvent, EnvType.CLIENT, 1),
    PLAY_TO_CLIENT(NetworkEvent::ServerCustomPayloadEvent, EnvType.SERVER, 0),
    LOGIN_TO_SERVER(NetworkEvent::ClientCustomPayloadLoginEvent, EnvType.CLIENT, 3),
    LOGIN_TO_CLIENT(NetworkEvent::ServerCustomPayloadLoginEvent, EnvType.SERVER, 2);

    fun reply(): NetworkDirection {
        return values()[response]
    }
}