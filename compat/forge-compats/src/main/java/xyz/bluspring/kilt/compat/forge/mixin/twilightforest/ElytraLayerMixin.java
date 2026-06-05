package xyz.bluspring.kilt.compat.forge.mixin.twilightforest;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.asmhooks.ArmorHooks;

@IfModLoaded("twilightforest")
@Mixin(ElytraLayer.class)
public class ElytraLayerMixin {

    // Re-implementation of https://github.com/TeamTwilight/twilightforest/blob/1.21.1/tf-asm/src/main/java/twilightforest/asm/transformers/armor/CancelElytraRenderingTransformer.java
    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "ELYTRA", field = "Lnet/minecraft/world/item/Items;ELYTRA:Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(ELYTRA)")
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    public boolean kilt$twilightforest$render(ItemStack itemStack, Item item, Operation<Boolean> original) {
        return ArmorHooks.cancelArmorRendering(original.call(itemStack, item), itemStack);
    }

}
