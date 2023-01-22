package xyz.bluspring.kilt.forgeinjects.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ICustomPacket;
import net.minecraftforge.network.LoginWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundCustomQueryPacket.class)
public abstract class ServerboundCustomQueryPacketInject implements ICustomPacket<ServerboundCustomQueryPacket> {
    @Shadow @Nullable public abstract FriendlyByteBuf getData();

    @Shadow public abstract int getTransactionId();

    @Override
    public @Nullable FriendlyByteBuf getInternalData() {
        return this.getData();
    }

    @Override
    public ResourceLocation getName() {
        return LoginWrapper.WRAPPER;
    }

    @Override
    public int getIndex() {
        return this.getTransactionId();
    }
}
