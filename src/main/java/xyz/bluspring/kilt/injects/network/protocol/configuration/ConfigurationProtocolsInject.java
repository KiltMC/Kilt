package xyz.bluspring.kilt.injects.network.protocol.configuration;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.network.protocol.common.ServerboundCustomPayloadPacketInjection;

@Mixin(ConfigurationProtocols.class)
public abstract class ConfigurationProtocolsInject {
    @ModifyExpressionValue(method = "method_56513", at = @At(value = "FIELD", target = "Lnet/minecraft/network/protocol/common/ServerboundCustomPayloadPacket;STREAM_CODEC:Lnet/minecraft/network/codec/StreamCodec;"), require = 0)
    private static StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> kilt$useProtocolAwareStreamCodec(StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> original) {
        return ServerboundCustomPayloadPacketInjection.kilt$getConfigStreamCodec(original);
    }
}
