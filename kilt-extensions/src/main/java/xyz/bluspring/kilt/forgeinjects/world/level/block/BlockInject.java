package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Block.class)
public abstract class BlockInject implements IForgeBlock {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;skipRendering(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", shift = At.Shift.BEFORE), method = "shouldRenderFace", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void kilt$handleRenderFace(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction, BlockPos blockPos2, CallbackInfoReturnable<Boolean> cir, BlockState blockState2) {
        if (blockState.skipRendering(blockState2, direction))
            cir.setReturnValue(false);
        else if (((IForgeBlockState) blockState).supportsExternalFaceHiding() && ((IForgeBlockState) blockState2).hidesNeighborFace(blockGetter, blockPos, blockState, direction))
            cir.setReturnValue(false);
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"), method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V")
    private static boolean kilt$checkRestoringBlockSnapshots(Level instance) {
        // TODO: how do i inject fields into stuff
        return !instance.isClientSide; //&& !((IForgeLevel) instance).restoringBlockSnapshots;
    }
}
