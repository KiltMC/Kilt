// TRACKED HASH: ef10a7f9b9dede87015d343cb54f4b5294ec1834
package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockInject extends BaseEntityBlock {
    protected BeehiveBlockInject(Properties properties) {
        super(properties);
    }

    @ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0))
    private boolean checkCanPerformAction(boolean original, @Local ItemStack stack) {
        return original || stack.canPerformAction(ItemAbilities.SHEARS_HARVEST);
    }
}