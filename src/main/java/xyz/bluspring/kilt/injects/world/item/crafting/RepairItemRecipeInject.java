package xyz.bluspring.kilt.injects.world.item.crafting;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeInject {
    @ModifyReturnValue(method = "canCombine", at = @At("RETURN"))
    private static boolean kilt$checkIsBothRepairable(boolean original, ItemStack first, ItemStack second) {
        return original && first.isRepairable() && second.isRepairable();
    }
}
