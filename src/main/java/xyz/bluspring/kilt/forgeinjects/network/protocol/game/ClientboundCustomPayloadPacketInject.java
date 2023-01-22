package xyz.bluspring.kilt.forgeinjects.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundCustomPayloadPacket.class)
public abstract class ClientboundCustomPayloadPacketInject implements ICustomPacket<ClientboundCustomPayloadPacket> {
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
        return Integer.MAX_VALUE;
    }
}
