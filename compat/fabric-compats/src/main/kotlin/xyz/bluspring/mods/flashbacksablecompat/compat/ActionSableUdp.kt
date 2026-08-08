package xyz.bluspring.mods.flashbacksablecompat.compat

import com.moulberry.flashback.Flashback
import com.moulberry.flashback.action.Action
import com.moulberry.flashback.playback.ReplayServer
import dev.ryanhcode.sable.network.tcp.SableTCPPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

abstract class ActionSableUdp<T : SableTCPPacket>(private val name: ResourceLocation, val streamCodec: StreamCodec<RegistryFriendlyByteBuf, T>) : Action {
    override fun name(): ResourceLocation = this.name

    override fun handle(replayServer: ReplayServer, buf: RegistryFriendlyByteBuf) {
        val packet = this.streamCodec.decode(buf)

        if (replayServer.isProcessingSnapshot)
            return

        val shouldSend = if (Flashback.isExporting())
            Flashback.EXPORT_JOB.currentTickDouble > 0
        else
            !replayServer.replayPaused

        if (shouldSend) {
            for (viewer in replayServer.replayViewers) {
                ServerPlayNetworking.send(viewer, packet)
            }
        }
    }
}
