// TRACKED HASH: 65eab7af6923cfe40b811ec9f2b77f27d0284455
package xyz.bluspring.kilt.injects.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.common.extensions.IFriendlyByteBufExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.network.FriendlyByteBufInjection;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufInject implements IFriendlyByteBufExtension, FriendlyByteBufInjection {
    @Shadow
    @Final
    private ByteBuf source;

    @Override
    public ByteBuf getSource() {
        return this.source;
    }
}