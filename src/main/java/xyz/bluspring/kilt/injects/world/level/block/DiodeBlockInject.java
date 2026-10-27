package xyz.bluspring.kilt.injects.world.level.block;

import java.util.EnumSet;

import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(DiodeBlock.class)
public abstract class DiodeBlockInject {
    @Inject(method = "updateNeighborsInFront", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V"), cancellable = true)
    private void kilt$callNotifyNeighborsEvent(Level level, BlockPos pos, BlockState state, CallbackInfo ci, @Local Direction direction) {
        if (EventHooks.onNeighborNotify(level, pos, state, EnumSet.of(direction.getOpposite()), false).isCanceled())
            ci.cancel();
    }
}
