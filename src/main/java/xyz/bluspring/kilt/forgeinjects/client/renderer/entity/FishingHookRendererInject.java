// TRACKED HASH: 77565619d9e3fb1b0af1612ca043da5909eb659a
package xyz.bluspring.kilt.forgeinjects.client.renderer.entity;

import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHookRenderer.class)
public class FishingHookRendererInject {
    @Redirect(at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"), method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public boolean kilt$checkRodUsingForge(ItemStack instance, Item item) {
        return instance.canPerformAction(ToolActions.FISHING_ROD_CAST);
    }
}