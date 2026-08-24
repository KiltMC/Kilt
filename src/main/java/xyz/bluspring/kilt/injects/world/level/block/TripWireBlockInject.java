package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TripWireBlock;

@Mixin(TripWireBlock.class)
public abstract class TripWireBlockInject {
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    @Definition(id = "SHEARS", field = "Lnet/minecraft/world/item/Items;SHEARS:Lnet/minecraft/world/item/Item;")
    @Expression("?.is(SHEARS)")
    @WrapOperation(method = "playerWillDestroy", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanDisarmBlock(ItemStack instance, Object item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.canPerformAction(ItemAbilities.SHEARS_DISARM);
    }
}
