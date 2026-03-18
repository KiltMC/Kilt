package xyz.bluspring.kilt.injects.client.renderer.entity.layers;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerInject {
    @Definition(id = "itemStack", local = @Local(type = ItemStack.class, argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "SPYGLASS", field = "Lnet/minecraft/world/item/Items;SPYGLASS:Lnet/minecraft/world/item/Item;")
    @Expression("itemStack.is(SPYGLASS)")
    @WrapOperation(method = "renderArmWithItem", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryCheckCanSpyglassScope(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.canPerformAction(ItemAbilities.SPYGLASS_SCOPE);
    }
}
