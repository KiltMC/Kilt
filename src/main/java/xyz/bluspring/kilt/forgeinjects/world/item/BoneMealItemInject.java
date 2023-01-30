package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.injections.item.BoneMealItemInjection;

@Mixin(BoneMealItem.class)
public class BoneMealItemInject implements BoneMealItemInjection {
    // Due to how Forge handles bone meal apply events, we need to replicate it here.
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0, shift = At.Shift.BEFORE), method = "growCrop", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void kilt$postBoneMealApplyEvent(ItemStack itemStack, Level level, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir, BlockState blockState) {
        if (kiltFired.get()) {
            kiltFired.set(false);
            return;
        }

        if (level instanceof ServerLevel) {
            var hook = ForgeEventFactory.onApplyBonemeal(FakePlayerFactory.getMinecraft((ServerLevel) level), level, blockPos, blockState, itemStack);
            if (hook != 0) {
                cir.setReturnValue(hook > 0);
                return;
            }

            return;
        }

        cir.setReturnValue(false);
    }
}
