package xyz.bluspring.kilt.forgeinjects.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
}
