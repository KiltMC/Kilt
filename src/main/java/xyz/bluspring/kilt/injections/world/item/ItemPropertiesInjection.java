package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.Item;

public interface ItemPropertiesInjection {
    default boolean getCanRepair() {
        throw new IllegalStateException();
    }

    default Item.Properties setNoRepair() {
        throw new IllegalStateException();
    }
}
