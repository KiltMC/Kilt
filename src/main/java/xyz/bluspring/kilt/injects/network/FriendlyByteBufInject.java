// TRACKED HASH: 65eab7af6923cfe40b811ec9f2b77f27d0284455
package xyz.bluspring.kilt.injects.network;

import io.netty.buffer.ByteBuf;
import net.neoforged.neoforge.common.extensions.IFriendlyByteBufExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.network.FriendlyByteBufInjection;

import net.minecraft.network.FriendlyByteBuf;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufInject implements IFriendlyByteBufExtension, FriendlyByteBufInjection {
    @Shadow @Final private ByteBuf source;

    // Kilt: Capacity size limiting implemented by Fabric API

    @Override
    public ByteBuf getSource() {
        return this.source;
    }
}
