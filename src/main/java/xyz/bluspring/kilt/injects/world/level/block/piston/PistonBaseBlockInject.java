package xyz.bluspring.kilt.injects.world.level.block.piston;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockInject extends DirectionalBlock {
    @Shadow @Final public static BooleanProperty EXTENDED;

    protected PistonBaseBlockInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonBaseBlock;moveBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z", ordinal = 0), cancellable = true)
    private void kilt$callPistonMoveEventExtended(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir, @Local Direction direction) {
        if (EventHooks.onPistonMovePre(level, pos, direction, true))
            cir.setReturnValue(false);
    }

    @Inject(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0), cancellable = true)
    private void kilt$callPistonMoveEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir, @Local Direction direction) {
        if (EventHooks.onPistonMovePre(level, pos, direction, false))
            cir.setReturnValue(false);
    }

    @Inject(method = "triggerEvent", at = @At("TAIL"))
    private void kilt$callPistonMovePostEvent(BlockState state, Level level, BlockPos pos, int b0, int b1, CallbackInfoReturnable<Boolean> cir) {
        EventHooks.onPistonMovePost(level, pos, state.getValue(FACING), b0 == 0);
    }

    @Definition(id = "level", local = @Local(type = Level.class, name = "level", argsOnly = true))
    @Definition(id = "setBlock", method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    @Definition(id = "pos", local = @Local(type = BlockPos.class, name = "pos"))
    @Definition(id = "AIR", field = "Lnet/minecraft/world/level/block/Blocks;AIR:Lnet/minecraft/world/level/block/Block;")
    @Definition(id = "defaultBlockState", method = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;")
    @Expression("level.setBlock(pos, AIR.defaultBlockState(), ?)")
    @WrapOperation(method = "moveBlocks", at = {@At("MIXINEXTRAS:EXPRESSION")})
    private boolean kilt$checkShouldHandleDestroyByPush(Level instance, BlockPos pos, BlockState state, int flags, Operation<Boolean> original, @Local(name = "direction", argsOnly = true) Direction direction, @Share("isCancelled") LocalBooleanRef isCancelled) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IBlockExtension.class, "onDestroyedByPushReaction", BlockState.class, Level.class, BlockPos.class, Direction.class, FluidState.class)) {
            state.getBlock().onDestroyedByPushReaction(state, instance, pos, direction, instance.getFluidState(pos));
            isCancelled.set(true);
            return false;
        }

        return original.call(instance, pos, state, flags);
    }

    @Definition(id = "level", local = @Local(type = Level.class, name = "level", argsOnly = true))
    @Definition(id = "gameEvent", method = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V")
    @Definition(id = "BLOCK_DESTROY", field = "Lnet/minecraft/world/level/gameevent/GameEvent;BLOCK_DESTROY:Lnet/minecraft/core/Holder$Reference;")
    @Definition(id = "pos", local = @Local(type = BlockPos.class, name = "pos"))
    @Definition(id = "of", method = "Lnet/minecraft/world/level/gameevent/GameEvent$Context;of(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/gameevent/GameEvent$Context;")
    @Definition(id = "state", local = @Local(type = BlockState.class, name = "state"))
    @Expression("level.gameEvent(BLOCK_DESTROY, pos, of(state))")
    @WrapWithCondition(method = "moveBlocks", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsHandledByPushReaction(Level instance, Holder holder, BlockPos blockPos, GameEvent.Context context, @Share("isCancelled") LocalBooleanRef isCancelled) {
        return !isCancelled.get();
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return state.getValue(EXTENDED) ? state : super.rotate(state, level, pos, direction);
    }
}
