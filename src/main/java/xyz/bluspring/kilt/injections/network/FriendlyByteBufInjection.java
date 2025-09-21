package xyz.bluspring.kilt.injections.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(FriendlyByteBuf.class)
public interface FriendlyByteBufInjection {
    @ApiStatus.Internal
    default ByteBuf getSource() {
        throw KiltHelper.createMixinException(FriendlyByteBufInjection.class, "getSource");
    }
}
