// TRACKED HASH: 79c23e6e87623076b1f9e335cc1c1deeaa796db4
package xyz.bluspring.kilt.forgeinjects.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketInject implements ICustomPacket<ClientboundCustomPayloadPacket> {
    @Shadow public abstract ResourceLocation getIdentifier();

    @Shadow public abstract FriendlyByteBuf getData();

    @Shadow @Final private FriendlyByteBuf data;
    @Unique
    private boolean shouldRelease;

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

    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    public void kilt$disableBufferRelease(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        this.shouldRelease = false;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("TAIL"))
    public void kilt$enableBufferRelease(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        this.shouldRelease = true;
    }

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;copy()Lio/netty/buffer/ByteBuf;"))
    public ByteBuf kilt$sliceData(FriendlyByteBuf instance) {
        return instance.slice();
    }

    @Inject(method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V", at = @At("TAIL"))
    public void kilt$releaseData(ClientGamePacketListener handler, CallbackInfo ci) {
        if (this.shouldRelease)
            this.data.release();
    }
}