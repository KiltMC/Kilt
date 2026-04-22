package xyz.bluspring.kilt.injects.network.protocol.common.custom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.StupidWorkarounds;
import xyz.bluspring.kilt.injections.network.protocol.common.custom.CustomPacketPayloadInjection;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@Mixin(CustomPacketPayload.class)
public interface CustomPacketPayloadInject extends CustomPacketPayloadInjection {
    @Mixin(targets = "net.minecraft.network.protocol.common.custom.CustomPacketPayload$1")
    abstract class CustomPacketPayloadAnonymous1Inject {
        @Unique private final ConnectionProtocol kilt$protocol = StupidWorkarounds.kilt$protocol.get();
        @Unique private final PacketFlow kilt$packetFlow = StupidWorkarounds.kilt$packetFlow.get();

        @WrapOperation(method = "findCodec", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;create(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/codec/StreamCodec;"))
        private <B extends FriendlyByteBuf> StreamCodec<? super B, ? extends CustomPacketPayload> kilt$tryCreateNeoCodecIfPossible(CustomPacketPayload.FallbackProvider<B> instance, ResourceLocation resourceLocation, Operation<StreamCodec<B, ? extends CustomPacketPayload>> original) {
            if (kilt$protocol == null || kilt$packetFlow == null)
                return original.call(instance, resourceLocation);

            var codec = NetworkRegistry.getCodec(resourceLocation, kilt$protocol, kilt$packetFlow);
            if (codec == null)
                return original.call(instance, resourceLocation);

            return codec;
        }

        /*@WrapOperation(method = "writeCap", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"))
        private void kilt$throwEncodeExceptionWithPayloadInfo(StreamCodec instance, Object o, Object p, Operation<Void> original, @Local(argsOnly = true) CustomPacketPayload.Type type) {
            try {
                original.call(instance, o, p);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed encoding custom payload " + type.id() + ": " + e, e);
            }
        }

        @WrapMethod(method = "decode(Ljava/lang/Object;)Ljava/lang/Object;")
        private Object kilt$throwDecodeExceptionWithPayloadInfo(Object object, Operation<Object> original) {
            try {
                original.call(object);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed decoding custom payload " + type.id() + ": " + e, e);
            }
            return null;
        }*/
    }

    @Override
    default ClientboundCustomPayloadPacket toVanillaClientbound() {
        return new ClientboundCustomPayloadPacket((CustomPacketPayload) this);
    }

    @Override
    default ServerboundCustomPayloadPacket toVanillaServerbound() {
        return new ServerboundCustomPayloadPacket((CustomPacketPayload) this);
    }
}
