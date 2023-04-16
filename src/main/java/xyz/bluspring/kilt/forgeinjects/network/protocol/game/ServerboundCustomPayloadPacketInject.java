package xyz.bluspring.kilt.forgeinjects.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeBytes(Lio/netty/buffer/ByteBuf;)Lio/netty/buffer/ByteBuf;"))
    public ByteBuf kilt$sliceData(FriendlyByteBuf instance, ByteBuf byteBuf) {
        return instance.writeBytes(byteBuf.slice());
    }
}
