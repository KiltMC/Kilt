// TRACKED HASH: 86b6d61165f028bef46e1c347032dc22100f0809
package xyz.bluspring.kilt.forgeinjects.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.RecipeBookManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookInject {
    @WrapOperation(method = "categorizeAndGroupRecipes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Recipe;getGroup()Ljava/lang/String;"))
    private static String kilt$defaultToRecipeIdIfGroupEmpty(Recipe<?> instance, Operation<String> original) {
        return original.call(instance).isEmpty() ? instance.getId().toString() : original.call(instance);
    }

    @Inject(method = "getCategory", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.BEFORE, remap = false), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void kilt$returnForgeCategories(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookCategories> cir) {
        var categories = RecipeBookManager.findCategories((RecipeType) recipe.getType(), recipe);
        if (categories != null)
            cir.setReturnValue(categories);
    }
}