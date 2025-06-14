package xyz.bluspring.kilt.forgeinjects.world.entity.monster.piglin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.StopHoldingItemIfNoLongerAdmiring;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StopHoldingItemIfNoLongerAdmiring.class)
public abstract class StopHoldingItemIfNoLongerAdmiringInject {
    @ModifyExpressionValue(method = "method_47299", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0))
    private static boolean kilt$checkCanPerformAction(boolean original, @Local(argsOnly = true) Piglin piglin) {
        return original || piglin.getOffhandItem().canPerformAction(ToolActions.SHIELD_BLOCK);
    }
}
