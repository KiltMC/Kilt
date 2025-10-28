package xyz.bluspring.kilt.injects.world.item.crafting;

import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.ShapedRecipePatternStorage;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.crafting.ShapedRecipePatternInjection;

@Mixin(ShapedRecipePattern.class)
public class ShapedRecipePatternInject implements ShapedRecipePatternInjection {
    static int maxWidth = 3;
    static int maxHeight = 3;

    @CreateStatic
    public static int getMaxWidth() {
        return ShapedRecipePatternStorage.getMaxWidth();
    }

    @CreateStatic
    public static int getMaxHeight() {
        return ShapedRecipePatternStorage.getMaxHeight();
    }

    @CreateStatic
    public static void setCraftingSize(int width, int height) {
        ShapedRecipePatternStorage.setCraftingSize(width, height);
        if (maxWidth < width) maxWidth = width;
        if (maxHeight < height) maxHeight = height;
    }
}
