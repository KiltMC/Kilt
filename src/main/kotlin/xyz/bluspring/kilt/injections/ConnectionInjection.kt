package xyz.bluspring.kilt.injections

import io.netty.channel.Channel
import net.minecraft.network.Connection
import net.minecraft.network.protocol.PacketFlow
import java.util.function.Consumer

interface ConnectionInjection {
    fun channel(): Channel
    fun getDirection(): PacketFlow
    fun setActivationHandler(handler: Consumer<Connection>)
}