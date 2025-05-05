package xyz.bluspring.kilt.forgeinjects.world.level.block.piston;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockInject extends DirectionalBlock {
    @Shadow @Final public static BooleanProperty EXTENDED;

    protected PistonBaseBlockInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonBaseBlock;moveBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z", ordinal = 0), cancellable = true)
    private void kilt$callPistonMoveEventExtended(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir, @Local Direction direction) {
        if (ForgeEventFactory.onPistonMovePre(level, pos, direction, true))
            cir.setReturnValue(false);
    }

    @Inject(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0), cancellable = true)
    private void kilt$callPistonMoveEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir, @Local Direction direction) {
        if (ForgeEventFactory.onPistonMovePre(level, pos, direction, false))
            cir.setReturnValue(false);
    }

    @Inject(method = "triggerEvent", at = @At("TAIL"))
    private void kilt$callPistonMovePostEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir) {
        ForgeEventFactory.onPistonMovePost(level, pos, state.getValue(FACING), id == 0);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return state.getValue(EXTENDED) ? state : super.rotate(state, level, pos, direction);
    }
}
