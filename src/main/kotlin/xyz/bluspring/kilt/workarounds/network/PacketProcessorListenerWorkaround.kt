package xyz.bluspring.kilt.workarounds.network

import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.DisconnectionDetails
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow
import net.neoforged.neoforge.network.handling.QueuedPacket

object PacketProcessorListenerWorkaround : PacketListener {
    override fun flow(): PacketFlow = PacketFlow.SERVERBOUND
    override fun protocol(): ConnectionProtocol = ConnectionProtocol.PLAY
    override fun onDisconnect(details: DisconnectionDetails) {}
    override fun isAcceptingMessages(): Boolean = false

    override fun shouldHandleMessage(packet: Packet<*>): Boolean {
        return packet is QueuedPacket
    }
}
