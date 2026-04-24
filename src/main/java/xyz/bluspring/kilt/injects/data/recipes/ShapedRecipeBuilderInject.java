package xyz.bluspring.kilt.injects.data.recipes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.data.recipes.ShapedRecipeBuilderInjection;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@Mixin(ShapedRecipeBuilder.class)
public abstract class ShapedRecipeBuilderInject implements ShapedRecipeBuilderInjection {
    @Unique private ItemStack resultStack;
    @Unique private boolean kilt$hasResultStack = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createResultStack(RecipeCategory category, ItemLike result, int count, CallbackInfo ci) {
        this.resultStack = new ItemStack(result, count);
    }

    public ShapedRecipeBuilderInject(RecipeCategory category, ItemLike result, int count) {}

    @CreateInitializer
    public ShapedRecipeBuilderInject(RecipeCategory category, ItemStack result) {
        this(category, result.getItem(), result.getCount());
        this.resultStack = result;
        this.kilt$hasResultStack = true;
    }

    @Override
    public void kilt$setResultStack(ItemStack result) {
        this.resultStack = result;
        this.kilt$hasResultStack = true;
    }

    @CreateStatic
    private static ShapedRecipeBuilder shaped(RecipeCategory category, ItemStack result) {
        var builder = new ShapedRecipeBuilder(category, result.getItem(), result.getCount());
        builder.kilt$setResultStack(result);
        return builder;
    }

    @WrapOperation(method = "save", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack kilt$tryUseResultStack(ItemLike item, int count, Operation<ItemStack> original) {
        if (this.kilt$hasResultStack) {
            return this.resultStack;
        }

        return original.call(item, count);
    }
}
