package xyz.bluspring.kilt.injections.client.color.item;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

import java.util.Map;

public interface ItemColorsInjection {
    Map<Holder.Reference<Item>, ItemColor> kilt$getItemColors();
}
