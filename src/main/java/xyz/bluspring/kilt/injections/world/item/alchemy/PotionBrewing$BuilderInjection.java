package xyz.bluspring.kilt.injections.world.item.alchemy;

import io.github.fabricators_of_create.porting_lib.brewing.ext.PotionBrewingBuilderExt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import xyz.bluspring.kilt.util.KiltHelper;

public interface PotionBrewing$BuilderInjection extends PotionBrewingBuilderExt {
    default void addRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        throw KiltHelper.createMixinException(PotionBrewing$BuilderInjection.class, "addRecipe");
    }

    default void addRecipe(IBrewingRecipe recipe) {
        throw KiltHelper.createMixinException(PotionBrewing$BuilderInjection.class, "addRecipe");
    }
}
