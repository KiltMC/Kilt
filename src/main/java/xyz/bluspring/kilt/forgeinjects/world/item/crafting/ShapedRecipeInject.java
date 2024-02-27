// TRACKED HASH: 6b717e608f1a84947c867222d601c601a3b66507
package xyz.bluspring.kilt.forgeinjects.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeInject {
    @Inject(at = @At("HEAD"), method = "itemStackFromJson", cancellable = true)
    private static void kilt$useForgeGetItemStack(JsonObject jsonObject, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(CraftingHelper.getItemStack(jsonObject, true, true));
    }
}