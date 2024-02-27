// TRACKED HASH: d5cb56b0b25a2fa35c8362c0cab8c890e3322c9b
package xyz.bluspring.kilt.forgeinjects.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RecipeBookManager;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.RecipeBookCategoriesInjection;

import java.util.List;
import java.util.Map;

@Mixin(RecipeBookCategories.class)
public class RecipeBookCategoriesInject implements RecipeBookCategoriesInjection, IExtensibleEnum {
    @Shadow @Final
    @Mutable
    public static Map<RecipeBookCategories, List<RecipeBookCategories>> AGGREGATE_CATEGORIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$useForgeRecipeCategories(CallbackInfo ci) {
        AGGREGATE_CATEGORIES = RecipeBookManager.getAggregateCategories();
    }

    @Inject(method = "getCategories", at = @At("RETURN"), cancellable = true)
    private static void kilt$useForgeCustomCategories(RecipeBookType recipeBookType, CallbackInfoReturnable<List<RecipeBookCategories>> cir) {
        if (cir.getReturnValue() instanceof ImmutableList<RecipeBookCategories> && cir.getReturnValue().isEmpty()) {
            cir.setReturnValue(RecipeBookManager.getCustomCategoriesOrEmpty(recipeBookType));
        }
    }

    @CreateStatic
    private static RecipeBookCategories create(String name, ItemStack... icons) {
        return RecipeBookCategoriesInjection.create(name, icons);
    }
}