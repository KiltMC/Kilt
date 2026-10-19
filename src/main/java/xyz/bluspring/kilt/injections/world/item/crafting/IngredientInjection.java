package xyz.bluspring.kilt.injections.world.item.crafting;

import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;

public interface IngredientInjection extends FabricIngredient {
    default boolean isSimple() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "isSimple");
    }

    default HolderSet<Item> getValues() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "getValues");
    }

    default @Nullable ICustomIngredient kilt$getCustomIngredient() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "kilt$getCustomIngredient");
    }

    default boolean isCustom() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "isCustom");
    }
}
