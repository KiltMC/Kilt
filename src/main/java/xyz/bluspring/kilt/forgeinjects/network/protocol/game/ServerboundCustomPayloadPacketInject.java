// TRACKED HASH: adec18f3b929ea00673738d3087382ec26b40492
package xyz.bluspring.kilt.forgeinjects.network.protocol.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerboundCustomPayloadPacket.class)
public abstract class ServerboundCustomPayloadPacketInject implements ICustomPacket<ServerboundCustomPayloadPacket> {
    @Shadow public abstract FriendlyByteBuf getData();

    @Shadow public abstract ResourceLocation getIdentifier();

    @Override
    public @Nullable FriendlyByteBuf getInternalData() {
        return this.getData();
    }

    @Override
    public ResourceLocation getName() {
        return this.getIdentifier();
    }

    @Override
    public int getIndex() {
        return Integer.MAX_VALUE;
    }

    @WrapOperation(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeBytes(Lio/netty/buffer/ByteBuf;)Lio/netty/buffer/ByteBuf;"))
    public ByteBuf kilt$sliceData(FriendlyByteBuf instance, ByteBuf byteBuf, Operation<ByteBuf> original) {
        return original.call(instance, byteBuf.slice());
    }
}