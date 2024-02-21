package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;

public interface DyeColorInjection {
    static DyeColor getColor(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem)
            return ((DyeItem) stack.getItem()).getDyeColor();

        for (int x = 0; x < DyeColor.BLACK.getId(); x++) {
            var dyeColor = DyeColor.byId(x);
            if (stack.is(dyeColor.getTag()))
                return dyeColor;
        }

        return null;
    }
}
