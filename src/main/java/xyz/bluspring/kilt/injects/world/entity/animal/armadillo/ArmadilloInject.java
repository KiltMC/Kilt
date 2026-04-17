package xyz.bluspring.kilt.injects.world.entity.animal.armadillo;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Armadillo.class)
public abstract class ArmadilloInject {
    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "BRUSH", field = "Lnet/minecraft/world/item/Items;BRUSH:Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(BRUSH)")
    @WrapOperation(method = "mobInteract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanBrushNeo(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.canPerformAction(ItemAbilities.BRUSH_BRUSH);
    }
}
