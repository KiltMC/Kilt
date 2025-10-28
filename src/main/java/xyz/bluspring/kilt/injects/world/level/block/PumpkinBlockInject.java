package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PumpkinBlock;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockInject {
    @ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean checkCanCarvePumpkin(boolean original, @Local(argsOnly = true) ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(stack.getItem().getClass(), Item.class, "canPerformAction", ItemStack.class, ItemAbility.class)) {
            return stack.canPerformAction(ItemAbilities.SHEARS_CARVE);
        }
        return original;
    }
}
