package xyz.bluspring.kilt.forgeinjects.network.protocol.status;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.network.protocol.status.ClientboundStatusResponsePacketInjection;

@Mixin(ClientboundStatusResponsePacket.class)
public abstract class ClientboundStatusResponsePacketInject implements ClientboundStatusResponsePacketInjection {
    @Nullable @Unique
    private String cachedStatus;

    @Unique
    private boolean kilt$shouldWriteStatus = false;

    public ClientboundStatusResponsePacketInject(ServerStatus status) {}

    @CreateInitializer
    public ClientboundStatusResponsePacketInject(ServerStatus status, @Nullable String cachedStatus) {
        this(status);
        this.kilt$setCachedStatus(cachedStatus);
        this.kilt$shouldWriteStatus = true;
    }

    @Override
    public @Nullable String cachedStatus() {
        return cachedStatus;
    }

    @Override
    public void kilt$setCachedStatus(String data) {
        this.cachedStatus = data;
    }

    @WrapOperation(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeJsonWithCodec(Lcom/mojang/serialization/Codec;Ljava/lang/Object;)V"))
    private <T> void kilt$writeCachedStatus(FriendlyByteBuf instance, Codec<T> codec, T value, Operation<Void> original) {
        if (this.cachedStatus != null && this.kilt$shouldWriteStatus)
            instance.writeUtf(cachedStatus);
        else
            //noinspection MixinExtrasOperationParameters
            original.call(instance, codec, value);
    }
}
