// TRACKED HASH: 77565619d9e3fb1b0af1612ca043da5909eb659a
package xyz.bluspring.kilt.injects.client.renderer.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHookRenderer.class)
public class FishingHookRendererInject {
    @ModifyExpressionValue(at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"), method = "getPlayerHandPos")
    public boolean kilt$checkRodUsingForge(boolean original, @Local ItemStack stack) {
        return original || stack.canPerformAction(ItemAbilities.FISHING_ROD_CAST);
    }
}