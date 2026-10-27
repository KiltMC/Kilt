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
    private static void kilt$storeBubbleColumnDirection(Block bubbleColumn, BlockState belowState, BlockState occupyState, CallbackInfoReturnable<BlockState> cir, @Share("bubbleColumnDirection") LocalRef<BubbleColumnDirection> bubbleColumnDirectionRef) {
        bubbleColumnDirectionRef.set(belowState.getBubbleColumnDirection());
    }

    @Definition(id = "belowState", local = @Local(type = BlockState.class, name = "belowState", argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z")
    @Definition(id = "ENABLES_BUBBLE_COLUMN_PUSH_UP", field = "Lnet/minecraft/tags/BlockTags;ENABLES_BUBBLE_COLUMN_PUSH_UP:Lnet/minecraft/tags/TagKey;")
    @Expression("belowState.is(ENABLES_BUBBLE_COLUMN_PUSH_UP)")
    @ModifyExpressionValue(method = "getColumnState", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$tryHandleUpwardColumn(boolean original, @Share("bubbleColumnDirection") LocalRef<BubbleColumnDirection> bubbleColumnDirectionRef) {
        return original || bubbleColumnDirectionRef.get() == BubbleColumnDirection.UPWARD;
    }

    @Definition(id = "belowState", local = @Local(type = BlockState.class, name = "belowState", argsOnly = true))
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z")
    @Definition(id = "ENABLES_BUBBLE_COLUMN_DRAG_DOWN", field = "Lnet/minecraft/tags/BlockTags;ENABLES_BUBBLE_COLUMN_DRAG_DOWN:Lnet/minecraft/tags/TagKey;")
    @Expression("belowState.is(ENABLES_BUBBLE_COLUMN_DRAG_DOWN)")
    @ModifyExpressionValue(method = "getColumnState", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$tryHandleDownwardColumn(boolean original, @Share("bubbleColumnDirection") LocalRef<BubbleColumnDirection> bubbleColumnDirectionRef) {
        return original || bubbleColumnDirectionRef.get() == BubbleColumnDirection.DOWNWARD;
    }
}
