package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import xyz.bluspring.kilt.mixin.DyeColorAccessor;

public interface DyeColorInjection {
    static DyeColor getColor(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem)
            return ((DyeItem) stack.getItem()).getDyeColor();

        for (DyeColor dyeColor : DyeColorAccessor.getById()) {
            if (stack.is(dyeColor.getTag()))
                return dyeColor;
        }

        return null;
    }
}
