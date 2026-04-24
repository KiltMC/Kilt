package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(MangrovePropaguleBlock.class)
public abstract class MangrovePropaguleBlockInject extends SaplingBlock {
    public MangrovePropaguleBlockInject(TreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    @Definition(id = "level", local = @Local(type = LevelReader.class, argsOnly = true))
    @Definition(id = "getBlockState", method = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    @Definition(id = "pos", local = @Local(type = BlockPos.class, argsOnly = true))
    @Definition(id = "above", method = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;")
    @Expression("level.getBlockState(pos.above())")
    @WrapOperation(method = "canSurvive", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState kilt$checkSoilDecision(LevelReader instance, BlockPos pos, Operation<BlockState> original, @Cancellable CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) BlockState plantState) {
        var state = original.call(instance, pos);
        var soilDecision = state.canSustainPlant(instance, pos, Direction.DOWN, plantState);
        if (!soilDecision.isDefault()) {
            cir.setReturnValue(soilDecision.isTrue());
        }

        return state;
    }
}
