// TRACKED HASH: ea137127081f8e3b35dd94c07fdca929038853ea
package xyz.bluspring.kilt.injects.data.recipes;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.data.recipes.RecipeProviderInjection;

import net.minecraft.data.recipes.RecipeProvider;

@Mixin(RecipeProvider.class)
public abstract class RecipeProviderInject implements RecipeProviderInjection {
}
