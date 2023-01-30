package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.item.crafting.RecipeManagerInjection;

@Mixin(RecipeManager.class)
public class RecipeManagerInject implements RecipeManagerInjection {
}
