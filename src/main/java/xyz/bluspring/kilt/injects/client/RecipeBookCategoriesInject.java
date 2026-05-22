// TRACKED HASH: d5cb56b0b25a2fa35c8362c0cab8c890e3322c9b
package xyz.bluspring.kilt.injects.client;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.ReservedConstructor;
import net.neoforged.neoforge.client.RecipeBookManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(RecipeBookCategories.class)
public class RecipeBookCategoriesInject implements IExtensibleEnum {
    @Shadow @Final
    @Mutable
    public static Map<RecipeBookCategories, List<RecipeBookCategories>> AGGREGATE_CATEGORIES;

    @Unique
    private Supplier<List<ItemStack>> kilt$itemIconsSupplier;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$useForgeRecipeCategories(CallbackInfo ci) {
        AGGREGATE_CATEGORIES = RecipeBookManager.getAggregateCategories();
    }

    @ReservedConstructor
    private RecipeBookCategoriesInject(String name, int ordinal, final ItemStack... itemStacks) {}

    @CreateInitializer
    private RecipeBookCategoriesInject(String name, int ordinal, Supplier<List<ItemStack>> itemIconsSupplier) {
        this(name, ordinal);
        this.kilt$itemIconsSupplier = itemIconsSupplier;
    }

    @ModifyReturnValue(method = "getIconItems", at = @At("RETURN"))
    public List<ItemStack> kilt$init(List<ItemStack> original) {
        if (kilt$itemIconsSupplier != null) return kilt$itemIconsSupplier.get();
        return original;
    }

    @WrapMethod(method = "getCategories", order = 1050)
    private static List<RecipeBookCategories> kilt$useForgeCustomCategories(
        RecipeBookType recipeBookType, Operation<List<RecipeBookCategories>> original
    ) {
        try {
            return original.call(recipeBookType);
        } catch (MatchException ignored) {
            return RecipeBookManager.getCustomCategoriesOrEmpty(recipeBookType);
        }
    }

    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(RecipeBookCategories.class);
    }
}
