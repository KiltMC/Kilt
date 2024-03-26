// TRACKED HASH: 6b717e608f1a84947c867222d601c601a3b66507
package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import io.github.fabricators_of_create.porting_lib.util.ShapedRecipeUtil;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.crafting.ShapedRecipeInjection;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeInject implements IShapedRecipe<CraftingContainer>, ShapedRecipeInjection {
    @Shadow public abstract int getWidth();

    @Shadow public abstract int getHeight();

    @CreateStatic
    private static void setCraftingSize(int width, int height) {
        ShapedRecipeUtil.setCraftingSize(width, height);
    }

    @Override
    public int getRecipeWidth() {
        return this.getWidth();
    }

    @Override
    public int getRecipeHeight() {
        return this.getHeight();
    }

    // handled by Porting Lib
    /*
    @ModifyConstant(method = "patternFromJson", constant = @Constant(intValue = 3, ordinal = 1, log = true))
    private static int kilt$useCustomMaxRecipeWidth(int constant) {
        if (constant > MAX_WIDTH) {
            MAX_WIDTH = constant;
        }

        return MAX_WIDTH;
    }

    @ModifyConstant(method = "patternFromJson", constant = @Constant(intValue = 3, ordinal = 0, log = true))
    private static int kilt$useCustomMaxRecipeHeight(int constant) {
        if (constant > MAX_HEIGHT) {
            MAX_HEIGHT = constant;
        }

        return MAX_HEIGHT;
    }*/

    // handled by Porting Lib
    // ..i think?
    /*@Inject(at = @At("HEAD"), method = "itemStackFromJson", cancellable = true)
    private static void kilt$useForgeGetItemStack(JsonObject jsonObject, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(CraftingHelper.getItemStack(jsonObject, true, true));
    }*/
}