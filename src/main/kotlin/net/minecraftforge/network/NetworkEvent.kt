package net.minecraftforge.network

import io.netty.util.Attribute
import io.netty.util.AttributeKey
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraftforge.eventbus.api.Event
import xyz.bluspring.kilt.injections.ConnectionInjection
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Supplier

open class NetworkEvent : Event {
    constructor(source: Supplier<Context>) {
        this.source = source
        payload = null
        loginIndex = -1
    }

    constructor(payload: FriendlyByteBuf?, source: Supplier<Context>, loginIndex: Int) {
        this.payload = payload
        this.source = source
        this.loginIndex = loginIndex
    }

    constructor(payload: ICustomPacket<*>, source: Supplier<Context>) {
        this.payload = payload.getInternalData()
        this.source = source
        this.loginIndex = payload.getIndex()
    }

    val source: Supplier<Context>
    val payload: FriendlyByteBuf?
    val loginIndex: Int

    open class ServerCustomPayloadEvent(payload: ICustomPacket<*>, source: Supplier<Context>) : NetworkEvent(payload, source)
    open class ClientCustomPayloadEvent(payload: ICustomPacket<*>, source: Supplier<Context>) : NetworkEvent(payload, source)
    open class ServerCustomPayloadLoginEvent(payload: ICustomPacket<*>, source: Supplier<Context>) : ServerCustomPayloadEvent(payload, source)
    open class ClientCustomPayloadLoginEvent(payload: ICustomPacket<*>, source: Supplier<Context>) : ClientCustomPayloadEvent(payload, source)

    open class GatherLoginPayloadsEvent(loginPayloadList: List<NetworkRegistry.LoginPayload>, val isLocal: Boolean) : Event() {
        private val collected = loginPayloadList.toMutableList()
        fun add(buffer: FriendlyByteBuf, channelName: ResourceLocation, context: String) {
            collected.add(LoginPayload(buffer, channelName, context))
        }

        fun add(buffer: FriendlyByteBuf, channelName: ResourceLocation, context: String, needsResponse: Boolean) {
            collected.add(LoginPayload(buffer, channelName, context, needsResponse))
        }
    }

    open class LoginPayloadEvent(payload: FriendlyByteBuf, source: Supplier<Context>, loginIndex: Int) : NetworkEvent(payload, source, loginIndex)

    enum class RegistrationChangeType {
        REGISTER, UNREGISTER
    }

    open class ChannelRegistrationChangeEvent(source: Supplier<Context>, val changeType: RegistrationChangeType) : NetworkEvent(source)

    class Context internal constructor(
        val networkManager: Connection,
        private val networkDirection: NetworkDirection,
        private val dispatcher: PacketDispatcher
    ) {
        var packetHandled: Boolean = false
        val direction: NetworkDirection
            get() { return networkDirection }

        val packetDispatcher: PacketDispatcher
            get() { return dispatcher }

        internal constructor(
            netHandler: Connection,
            networkDirection: NetworkDirection,
            index: Int
        ) : this(netHandler, networkDirection, PacketDispatcher.NetworkManagerDispatcher(netHandler, index, networkDirection.reply()::buildPacket))

        internal constructor(
            networkManager: Connection,
            networkDirection: NetworkDirection,
            packetSink: BiConsumer<ResourceLocation, FriendlyByteBuf>
        ) : this(networkManager, networkDirection, PacketDispatcher(packetSink))

        fun <T> attr(key: AttributeKey<T>): Attribute<T> {
            return (networkManager as ConnectionInjection).channel().attr(key)
        }

        fun enqueueWork(runnable: Runnable): CompletableFuture<Void> {
            val executor = LogicalSidedProvider.WORKQUEUE.get(direction.recipientSide)

            return if (!executor.isSameThread())
                executor.submitAsync(runnable)
            else {
                runnable.run()
                return CompletableFuture.completedFuture(null)
            }
        }

        fun getSender(): ServerPlayer? {
            val netHandler = networkManager.packetListener
            if (netHandler is ServerGamePacketListenerImpl) {
                return netHandler.getPlayer()
            }

            return null
        }
    }

    open class PacketDispatcher internal constructor(
        private val packetSink: BiConsumer<ResourceLocation, FriendlyByteBuf>
    ) {
        fun sendPacket(resourceLocation: ResourceLocation, buffer: FriendlyByteBuf) {
            packetSink.accept(resourceLocation, buffer)
        }

        class NetworkManagerDispatcher(
            private val manager: Connection,
            private val packetIndex: Int,
            private val customPacketSupplier: BiFunction<Pair<FriendlyByteBuf, Int>, ResourceLocation, ICustomPacket<*>>
        ) : PacketDispatcher(BiConsumer { resourceLocation, buffer ->
            val packet = customPacketSupplier.apply(Pair(buffer, packetIndex), resourceLocation)
            manager.send(packet.getThis())
        })
    }
}