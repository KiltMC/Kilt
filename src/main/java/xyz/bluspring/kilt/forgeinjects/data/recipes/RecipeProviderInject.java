package xyz.bluspring.kilt.forgeinjects.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.data.recipes.RecipeProviderInjection;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

@Mixin(RecipeProvider.class)
public abstract class RecipeProviderInject implements RecipeProviderInjection {
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/recipes/RecipeProvider;saveAdvancement(Lnet/minecraft/data/CachedOutput;Lcom/google/gson/JsonObject;Ljava/nio/file/Path;)V"), cancellable = true)
    private void kilt$disableRunningOnVanilla(CachedOutput output, CallbackInfo ci) {
        if (((Object) this).getClass() == RecipeProvider.class)
            ci.cancel();
    }

    @Redirect(method = {"run", "method_10421"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/data/recipes/RecipeProvider;saveAdvancement(Lnet/minecraft/data/CachedOutput;Lcom/google/gson/JsonObject;Ljava/nio/file/Path;)V"))
    private void kilt$useOverridableMethod(CachedOutput output, JsonObject advancementJson, Path path) {
        this.kilt$saveAdvancement(output, advancementJson, path);
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/recipes/RecipeProvider;buildCraftingRecipes(Ljava/util/function/Consumer;)V"))
    private void kilt$useOverridableMethod(Consumer<FinishedRecipe> finishedRecipeConsumer) {
        this.kilt$buildCraftingRecipes(finishedRecipeConsumer);
    }
}
