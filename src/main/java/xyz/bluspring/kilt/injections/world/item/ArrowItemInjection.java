package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ArrowItemInjection {
    boolean isInfinite(ItemStack stack, ItemStack bow, Player player);
}
