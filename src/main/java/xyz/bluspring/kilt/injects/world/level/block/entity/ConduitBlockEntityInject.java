package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

@Mixin(ConduitBlockEntity.class)
public abstract class ConduitBlockEntityInject {
    @Shadow @Final private static Block[] VALID_BLOCKS;

    @Inject(method = "updateShape", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/ConduitBlockEntity;VALID_BLOCKS:[Lnet/minecraft/world/level/block/Block;"))
    private static void kilt$addPosIfConduitFrame(Level level, BlockPos pos, List<BlockPos> positions, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) BlockPos pos2, @Local BlockState state) {
        if (state.isConduitFrame(level, pos2, pos) && Arrays.stream(VALID_BLOCKS).noneMatch(e -> state.getBlock() == e)) {
            positions.add(pos2);
        }
    }
}
