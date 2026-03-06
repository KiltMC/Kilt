package xyz.bluspring.kilt.injects.server.network;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.world.LevelChunkAuxiliaryLightManager;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderInject {
    @ModifyArg(method = "sendChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private static Packet<?> kilt$trySendLightData(Packet<?> original, @Local(argsOnly = true) LevelChunk chunk) {
        if (original instanceof ClientboundLevelChunkWithLightPacket lightPacket) {
            return ((LevelChunkAuxiliaryLightManager) chunk.getAuxLightManager(chunk.getPos())).sendLightDataTo(lightPacket);
        }

        return original;
    }

    @Inject(method = "sendChunk", at = @At("TAIL"))
    private static void kilt$handleChunkSentEvent(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelChunk chunk, CallbackInfo ci) {
        EventHooks.fireChunkSent(packetListener.player, chunk, level);
    }
}
