package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.item.crafting.RecipeTypeInjection;

@Mixin(RecipeType.class)
public interface RecipeTypeInject extends RecipeTypeInjection {
}
