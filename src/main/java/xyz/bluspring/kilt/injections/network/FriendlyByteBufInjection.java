package xyz.bluspring.kilt.injections.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface FriendlyByteBufInjection {
    FriendlyByteBuf writeItemStack(ItemStack stack, boolean limitedTag);
}
