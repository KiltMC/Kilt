// TRACKED HASH: a67a4f20572a775babd74519f3b68377454563f5
package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityInject {
    @ModifyReturnValue(method = "canPlaceItemThroughFace", at = @At("RETURN"))
    private boolean kilt$checkIfItemCanFitInContainerItems(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original && stack.getItem().canFitInsideContainerItems();
    }
}