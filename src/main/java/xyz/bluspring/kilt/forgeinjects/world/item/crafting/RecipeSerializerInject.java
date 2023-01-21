package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.extensions.IForgeRecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeSerializer.class)
public interface RecipeSerializerInject<T extends Recipe<?>> extends IForgeRecipeSerializer<T> {
}
