package xyz.bluspring.kilt.mixin.compat.fabric_api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(FabricItem.class)
public interface FabricItemMixin {
    @WrapOperation(method = "getRecipeRemainder", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultInstance()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack kilt$fabric_api$useForgeRecipeRemainderWherePossible(Item instance, Operation<ItemStack> original, @Local(argsOnly = true) ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), IForgeItem.class, "getCraftingRemainingItem", ItemStack.class)) {
            return ((Item) this).getCraftingRemainingItem(stack);
        }

        return original.call(instance);
    }
}
