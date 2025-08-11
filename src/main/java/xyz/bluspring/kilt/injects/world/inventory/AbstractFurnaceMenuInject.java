package xyz.bluspring.kilt.injects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceMenu.class)
public abstract class AbstractFurnaceMenuInject {
    @Shadow @Final private RecipeType<? extends AbstractCookingRecipe> recipeType;

    @ModifyReturnValue(method = "isFuel", at = @At("RETURN"))
    private boolean kilt$checkIfIsFuel(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || stack.getBurnTime(this.recipeType) > 0;
    }
}
