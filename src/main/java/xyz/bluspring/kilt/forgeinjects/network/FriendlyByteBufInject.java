// TRACKED HASH: 65eab7af6923cfe40b811ec9f2b77f27d0284455
package xyz.bluspring.kilt.forgeinjects.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufInject implements IForgeFriendlyByteBuf {
}