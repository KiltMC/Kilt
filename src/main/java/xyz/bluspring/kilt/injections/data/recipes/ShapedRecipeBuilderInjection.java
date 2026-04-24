package xyz.bluspring.kilt.injections.data.recipes;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.item.ItemStack;

public interface ShapedRecipeBuilderInjection {
    default void kilt$setResultStack(ItemStack result) {
        throw KiltHelper.createMixinException(ShapedRecipeBuilderInjection.class, "kilt$setResultStack");
    }
}
