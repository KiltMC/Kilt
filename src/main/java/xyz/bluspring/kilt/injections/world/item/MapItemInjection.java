package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public interface MapItemInjection {
    MapItemSavedData getCustomMapData(ItemStack stack, Level level);
}
