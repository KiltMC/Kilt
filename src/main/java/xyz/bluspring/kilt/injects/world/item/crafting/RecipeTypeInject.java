// TRACKED HASH: b6a7b3f4dcd24203dabc8d78fcec1330affbe661
package xyz.bluspring.kilt.injects.world.item.crafting;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.crafting.RecipeTypeInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

@Mixin(RecipeType.class)
public interface RecipeTypeInject extends RecipeTypeInjection {
    @CreateStatic
    private static <T extends Recipe<?>> RecipeType<T> simple(Identifier name) {
        return RecipeTypeInjection.simple(name);
    }
}
