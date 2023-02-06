package xyz.bluspring.kilt.mixin;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookCategories.class)
public interface RecipeBookCategoriesAccessor {
    @Invoker("<init>")
    static RecipeBookCategories createRecipeBookCategories(String internalName, int internalId, ItemStack... itemStacks) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    static RecipeBookCategories[] getValues() {
        throw new IllegalStateException();
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor("$VALUES")
    @Mutable
    static void setValues(RecipeBookCategories[] values) {
        throw new IllegalStateException();
    }
}
