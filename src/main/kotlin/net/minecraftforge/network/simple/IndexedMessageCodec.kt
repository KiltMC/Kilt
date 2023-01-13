package net.minecraftforge.network.simple

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap
import net.minecraftforge.network.NetworkInstance

// TODO: If something actually uses this class, let me know. Other than that, this seems pretty useless.
class IndexedMessageCodec(private val instance: NetworkInstance? = null) {
    private val types = Object2ObjectArrayMap<Class<*>, MessageHandler<*>>()
    private val indices = Short2ObjectArrayMap<MessageHandler<*>>()

    fun <MSG> findMessageType(msgToReply: MSG): MessageHandler<MSG> {
        return types[msgToReply!!::class.java] as MessageHandler<MSG>
    }

    internal fun <MSG> findIndex(i: Short): MessageHandler<MSG> {
        return indices[i] as MessageHandler<MSG>
    }

    class MessageHandler<MSG> {

    }
}