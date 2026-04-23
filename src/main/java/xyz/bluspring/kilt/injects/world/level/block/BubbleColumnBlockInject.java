package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BubbleColumnBlock.class)
public abstract class BubbleColumnBlockInject extends Block {
    public BubbleColumnBlockInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "getColumnState", at = @At("HEAD"))
    private static void kilt$storeBubbleColumnDirection(BlockState state, CallbackInfoReturnable<BlockState> cir, @Share("bubbleColumnDirection") LocalRef<BubbleColumnDirection> bubbleColumnDirectionRef) {
        bubbleColumnDirectionRef.set(state.getBubbleColumnDirection());
    }

    @Definition(id = "blockState", local = @Local(type = BlockState.class, argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "SOUL_SAND", field = "Lnet/minecraft/world/level/block/Blocks;SOUL_SAND:Lnet/minecraft/world/level/block/Block;")
    @Expression("blockState.is(SOUL_SAND)")
    @ModifyExpressionValue(method = "getColumnState", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$tryHandleUpwardColumn(boolean original, @Share("bubbleColumnDirection") LocalRef<BubbleColumnDirection> bubbleColumnDirectionRef) {
        return original || bubbleColumnDirectionRef.get() == BubbleColumnDirection.UPWARD;
    }

    @Definition(id = "blockState", local = @Local(type = BlockState.class, argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "MAGMA_BLOCK", field = "Lnet/minecraft/world/level/block/Blocks;MAGMA_BLOCK:Lnet/minecraft/world/level/block/Block;")
    @Expression("blockState.is(MAGMA_BLOCK)")
    @ModifyExpressionValue(method = "getColumnState", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$tryHandleDownwardColumn(boolean original, @Share("bubbleColumnDirection") LocalRef<BubbleColumnDirection> bubbleColumnDirectionRef) {
        return original || bubbleColumnDirectionRef.get() == BubbleColumnDirection.DOWNWARD;
    }
}
