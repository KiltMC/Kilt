package net.minecraftforge.network.simple

import io.netty.buffer.Unpooled
import me.pepperbell.simplenetworking.C2SPacket
import me.pepperbell.simplenetworking.S2CPacket
import net.fabricmc.api.EnvType
import net.minecraft.client.Minecraft
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkInstance
import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Supplier

class SimpleChannel private constructor(
    private val instance: NetworkInstance,
    private val registryChangeNotify: Optional<Consumer<NetworkEvent.ChannelRegistrationChangeEvent>>
) {
    private val indexedCodec = IndexedMessageCodec(instance)
    private val loginPackets = mutableListOf<java.util.function.Function<Boolean, List<Pair<String, Any>>>>()
    private val packetsNeedResponse = mutableMapOf<Class<*>, Boolean>()

    private val fabricSimpleChannel = me.pepperbell.simplenetworking.SimpleChannel(instance.channelName)

    constructor(instance: NetworkInstance) : this(instance, Optional.empty())
    constructor(instance: NetworkInstance, registryChangeNotify: Consumer<NetworkEvent.ChannelRegistrationChangeEvent>) : this(instance, Optional.of(registryChangeNotify))

    init {
        instance.addListener(this::networkEventListener)
        instance.addGatherListener(this::networkLoginGather)
    }

    private fun networkLoginGather(gatherEvent: NetworkEvent.GatherLoginPayloadsEvent) {
        loginPackets.forEach { packetGenerator ->
            packetGenerator.apply(gatherEvent.isLocal).forEach {
                val byteBuf = FriendlyByteBuf(Unpooled.buffer())
                indexedCodec.build(it.second, byteBuf)
                gatherEvent.add(byteBuf, instance.channelName, it.first, packetsNeedResponse.getOrDefault(it.second.javaClass, true))
            }
        }
    }

    private fun networkEventListener(networkEvent: NetworkEvent) {
        if (networkEvent is NetworkEvent.ChannelRegistrationChangeEvent) {
            registryChangeNotify.ifPresent {
                it.accept(networkEvent)
            }
        } else {
            indexedCodec.consume(networkEvent.payload, networkEvent.loginIndex, networkEvent.source)
        }
    }

    fun <MSG> encodeMessage(message: MSG, target: FriendlyByteBuf): Int {
        return indexedCodec.build(message, target)
    }

    fun <MSG> registerMessage(index: Int, messageType: Class<MSG>,
                              encoder: BiConsumer<MSG, FriendlyByteBuf>,
                              decoder: java.util.function.Function<FriendlyByteBuf, MSG>,
                              messageConsumer: BiConsumer<MSG, Supplier<NetworkEvent.Context>>
    ): IndexedMessageCodec.MessageHandler<MSG> {
        return registerMessage(index, messageType, encoder, decoder, messageConsumer, Optional.empty())
    }

    fun <MSG> registerMessage(index: Int, messageType: Class<MSG>,
                              encoder: BiConsumer<MSG, FriendlyByteBuf>,
                              decoder: java.util.function.Function<FriendlyByteBuf, MSG>,
                              messageConsumer: BiConsumer<MSG, Supplier<NetworkEvent.Context>>,
                              networkDirection: Optional<NetworkDirection>
    ): IndexedMessageCodec.MessageHandler<MSG> {
        return indexedCodec.addCodecIndex(index, messageType, encoder, decoder, messageConsumer, networkDirection)
    }

    fun <MSG> toBuffer(msg: MSG): Pair<FriendlyByteBuf, Int> {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        val index = encodeMessage(msg, buf)
        return Pair(buf, index)
    }

    fun <MSG> sendToServer(message: MSG) {
        sendTo(message, Minecraft.getInstance().connection!!.connection, NetworkDirection.PLAY_TO_SERVER)
    }

    fun <MSG> sendTo(message: MSG, manager: Connection, direction: NetworkDirection) {
        manager.send(toVanillaPacket(message, direction)!!)
    }

    fun <MSG> send(target: PacketDistributor.PacketTarget, message: MSG) {
        target.send(toVanillaPacket(message, target.direction))
    }

    fun <MSG> toVanillaPacket(message: MSG, direction: NetworkDirection): Packet<*>? {
        return if (direction.side == EnvType.CLIENT)
            fabricSimpleChannel.createVanillaPacket(message as C2SPacket)
        else
            fabricSimpleChannel.createVanillaPacket(message as S2CPacket)
    }

    fun <MSG> reply(msgToReply: MSG, context: NetworkEvent.Context) {
        context.packetDispatcher.sendPacket(instance.channelName, toBuffer(msgToReply).first)
    }

    fun isRemotePresent(manager: Connection): Boolean {
        return instance.isRemotePresent(manager)
    }
}