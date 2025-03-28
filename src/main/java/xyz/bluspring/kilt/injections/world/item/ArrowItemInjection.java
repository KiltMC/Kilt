package xyz.bluspring.kilt.injections.world.item;

import io.github.fabricators_of_create.porting_lib.item.InfiniteArrowItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ArrowItemInjection extends InfiniteArrowItem {
    boolean isInfinite(ItemStack stack, ItemStack bow, Player player);
}
