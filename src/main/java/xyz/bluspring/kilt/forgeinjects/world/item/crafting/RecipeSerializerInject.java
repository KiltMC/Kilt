// TRACKED HASH: 111e016c1d98773c26cba25471549d033348dc7e
package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.extensions.IForgeRecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeSerializer.class)
public interface RecipeSerializerInject<T extends Recipe<?>> extends IForgeRecipeSerializer<T> {
}