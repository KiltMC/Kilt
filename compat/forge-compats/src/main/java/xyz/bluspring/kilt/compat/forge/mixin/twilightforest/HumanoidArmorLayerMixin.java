package xyz.bluspring.kilt.compat.forge.mixin.twilightforest;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.asmhooks.ArmorHooks;

@IfModLoaded("twilightforest")
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    // Re-implementation of https://github.com/TeamTwilight/twilightforest/blob/1.21.1/tf-asm/src/main/java/twilightforest/asm/transformers/armor/CancelArmorRenderingTransformer.java
    @Definition(id = "ArmorItem", type = ArmorItem.class)
    @Expression("? instanceof ArmorItem")
    @ModifyExpressionValue(
        method = "renderArmorPiece",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    public boolean kilt$twilightforest$renderArmorPiece(boolean original, @Local ItemStack stack) {
        return ArmorHooks.cancelArmorRendering(original, stack);
    }

}
