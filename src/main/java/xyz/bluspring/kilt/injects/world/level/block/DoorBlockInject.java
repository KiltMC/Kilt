package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(DoorBlock.class)
public abstract class DoorBlockInject extends Block {
    public DoorBlockInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(method = "playerWillDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean kilt$tryUseNeoToolDrops(Player instance, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(instance.getClass(), Player.class, "hasCorrectToolForDrops", boolean.class, BlockState.class, Level.class, BlockPos.class)) {
            return instance.hasCorrectToolForDrops(state, level, pos);
        }

        return EventHooks.kilt$doPlayerHarvestCheck(instance, state, level, pos, original.call(instance, state));
    }
}
