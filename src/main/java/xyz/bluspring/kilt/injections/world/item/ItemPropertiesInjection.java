package xyz.bluspring.kilt.injections.world.item;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.item.Item;

public interface ItemPropertiesInjection {
    default Item.Properties setNoCombineRepair() {
        throw KiltHelper.createMixinException(ItemPropertiesInjection.class, "setNoCombineRepair");
    }

    default boolean kilt$canCombineRepair() {
        throw KiltHelper.createMixinException(ItemPropertiesInjection.class, "kilt$canCombineRepair");
    }
}
