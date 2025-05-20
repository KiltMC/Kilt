package xyz.bluspring.kilt.forgeinjects.world.entity.decoration;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorStand.class)
public abstract class ArmorStandInject {
    @Definition(id = "getMinecartType", method = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getMinecartType()Lnet/minecraft/world/entity/vehicle/AbstractMinecart$Type;")
    @Definition(id = "RIDEABLE", field = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart$Type;RIDEABLE:Lnet/minecraft/world/entity/vehicle/AbstractMinecart$Type;")
    @Expression("?.getMinecartType() == RIDEABLE")
    @ModifyExpressionValue(method = "method_6918", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$checkCanMinecartBeRidden(boolean original, @Local(argsOnly = true) Entity entity) {
        return original || ((AbstractMinecart) entity).canBeRidden();
    }
}
