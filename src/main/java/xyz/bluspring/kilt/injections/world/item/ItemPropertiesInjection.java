package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.Item;
import xyz.bluspring.kilt.mixin.ItemPropertiesAccessor;

public interface ItemPropertiesInjection {
    default boolean getCanRepair() {
        throw new IllegalStateException();
    }

    default Item.Properties setNoRepair() {
        throw new IllegalStateException();
    }

    static DataComponentMap validateComponents(DataComponentMap map) {
        // Kilt: We want to be able to have other mods' mixins, so let's recreate what this patch basically does.
        var properties = new Item.Properties();
        for (TypedDataComponent<?> component : map) {
            properties.component((DataComponentType) component.type(), component.value());
        }

        return ((ItemPropertiesAccessor) properties).callBuildAndValidateComponents();
    }
}
