package xyz.bluspring.kilt.injections

import io.netty.channel.Channel
import net.minecraft.network.Connection
import net.minecraft.network.protocol.PacketFlow
import java.util.function.Consumer

interface ConnectionInjection {
    fun channel(): Channel {
        throw RuntimeException("mixin, why didn't you add this")
    }

    fun getDirection(): PacketFlow {
        throw RuntimeException("mixin, why didn't you add this")
    }

    fun setActivationHandler(handler: Consumer<Connection>) {
        throw RuntimeException("mixin, why didn't you add this")
    }
}