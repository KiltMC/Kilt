package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.client.RecipeBookManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookInject {
    @ModifyExpressionValue(method = "categorizeAndGroupRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Recipe;getGroup()Ljava/lang/String;"))
    private static String kilt$useDefaultGroup(String original, @Local RecipeHolder<?> holder) {
        return original == null || original.isEmpty() ? holder.id().toString() : original;
    }

    @Inject(method = "getCategory", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), cancellable = true)
    private static void kilt$tryFindRecipeCategories(RecipeHolder<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {
        var categories = RecipeBookManager.findCategories((RecipeType) recipe.value().getType(), recipe);
        if (categories != null) {
            cir.setReturnValue(categories);
        }
    }
}
