package xyz.bluspring.kilt.forgeinjects.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufInject implements IForgeFriendlyByteBuf {
}
