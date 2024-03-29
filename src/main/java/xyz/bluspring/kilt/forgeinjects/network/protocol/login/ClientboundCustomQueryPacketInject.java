// TRACKED HASH: 7ad6a1ef71f97b3c75df95ec53149f1d5292d31e
package xyz.bluspring.kilt.forgeinjects.network.protocol.login;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundCustomQueryPacket.class)
public abstract class ClientboundCustomQueryPacketInject implements ICustomPacket<ClientboundCustomQueryPacket> {
    @Shadow public abstract int getTransactionId();

    @Shadow public abstract ResourceLocation getIdentifier();

    @Shadow public abstract FriendlyByteBuf getData();

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
        return this.getTransactionId();
    }

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;copy()Lio/netty/buffer/ByteBuf;"))
    public ByteBuf kilt$sliceData(FriendlyByteBuf instance) {
        return instance.slice();
    }
}