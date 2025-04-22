package xyz.bluspring.kilt.forgeinjects.network.protocol.status;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.network.protocol.status.ClientboundStatusResponsePacketInjection;

@Mixin(ClientboundStatusResponsePacket.class)
public abstract class ClientboundStatusResponsePacketInject implements ClientboundStatusResponsePacketInjection {
    @Shadow @Final private ServerStatus status;
    @Nullable @Unique
    private String cachedStatus;

    public ClientboundStatusResponsePacketInject(ServerStatus status) {}

    @CreateInitializer
    public ClientboundStatusResponsePacketInject(ServerStatus status, @Nullable String cachedStatus) {
        this(status);
        this.kilt$setCachedStatus(cachedStatus);
    }

    @Override
    public @Nullable String cachedStatus() {
        return cachedStatus;
    }

    @Override
    public void kilt$setCachedStatus(String data) {
        this.cachedStatus = data;
    }

    @WrapOperation(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeUtf(Ljava/lang/String;)Lnet/minecraft/network/FriendlyByteBuf;"))
    private <T> FriendlyByteBuf kilt$writeCachedStatus(FriendlyByteBuf instance, String string, Operation<FriendlyByteBuf> original) {
        return original.call(instance, this.status.getJson());
    }
}
