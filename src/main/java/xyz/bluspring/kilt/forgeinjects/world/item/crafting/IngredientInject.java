package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.item.crafting.IngredientInjection;

@Mixin(Ingredient.class)
public class IngredientInject implements IngredientInjection {
    @Override
    public boolean isVanilla() {
        return this.getClass().getPackageName().startsWith("net.minecraft");
    }
}
