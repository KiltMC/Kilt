// TRACKED HASH: b1d270381307f1dd2174ce194ca2605380b6ed59
package xyz.bluspring.kilt.forgeinjects.network.protocol.handshake;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.network.ClientIntentionPacketInjection;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketInject implements ClientIntentionPacketInjection {
    @Shadow @Final @Mutable
    private String hostName;
    @Unique
    private String fmlVersion = NetworkConstants.NETVERSION;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V")
    public void kilt$readForgeData(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        this.fmlVersion = NetworkHooks.getFMLVersion(this.hostName);
        this.hostName = this.hostName.split("\0")[0];
    }
    
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeUtf(Ljava/lang/String;)Lnet/minecraft/network/FriendlyByteBuf;", ordinal = 0))
    public String kilt$appendForgeNetworkVersion(String string) {
        return string + "\0" + NetworkConstants.NETVERSION + "\0";
    }

    public String getFmlVersion() {
        return fmlVersion;
    }
}