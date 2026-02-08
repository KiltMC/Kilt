package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import xyz.bluspring.kilt.util.KiltHelper;

public interface MapItemInjection {
    default MapItemSavedData getCustomMapData(ItemStack stack, Level level) {
        throw KiltHelper.createMixinException(MapItemInjection.class, "getCustomMapData");
    }
}
