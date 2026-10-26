package xyz.bluspring.kilt.injections.world.item;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface DyeColorInjection {
    default TagKey<Item> getTag() {
        throw KiltHelper.createMixinException(DyeColorInjection.class, "getTag");
    }

    default TagKey<Item> getDyedTag() {
        throw KiltHelper.createMixinException(DyeColorInjection.class, "getDyedTag");
    }

    static DyeColor getColor(ItemStack stack) {
        DyeColor dyeColor = stack.get(DataComponents.DYE);
        if (dyeColor != null)
            return dyeColor;

        for (int x = 0; x < DyeColor.BLACK.getId(); x++) {
            dyeColor = DyeColor.byId(x);
            if (stack.is(dyeColor.getTag()))
                return dyeColor;
        }

        return null;
    }
}
