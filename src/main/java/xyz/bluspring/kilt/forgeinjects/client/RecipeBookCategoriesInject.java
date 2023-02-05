package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import xyz.bluspring.kilt.injections.client.RecipeBookCategoriesInjection;

@Mixin(RecipeBookCategories.class)
public class RecipeBookCategoriesInject implements RecipeBookCategoriesInjection, IExtensibleEnum {

}
