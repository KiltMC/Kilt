package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.ArmorItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorItem.class)
public abstract class ArmorItemInject {
    @Shadow @Final protected float knockbackResistance;

    @Definition(id = "NETHERITE", field = "Lnet/minecraft/world/item/ArmorMaterials;NETHERITE:Lnet/minecraft/world/item/ArmorMaterials;")
    @Expression("? == NETHERITE")
    @ModifyExpressionValue(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkHasKnockbackResistance(boolean original) {
        return original || this.knockbackResistance > 0;
    }
}
