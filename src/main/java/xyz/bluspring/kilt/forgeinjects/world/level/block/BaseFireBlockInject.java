package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockInject {
    @WrapOperation(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/PortalShape;findEmptyPortalShape(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"))
    private Optional<PortalShape> kilt$trySpawnPortalEvent(LevelAccessor level, BlockPos bottomLeft, Direction.Axis axis, Operation<Optional<PortalShape>> original) {
        return ForgeEventFactory.onTrySpawnPortal(level, bottomLeft, original.call(level, bottomLeft, axis));
    }

    @WrapOperation(method = "isPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean kilt$checkIfPortalFrame(BlockState instance, Block block, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local BlockPos.MutableBlockPos pos) {
        return original.call(instance, block) || instance.isPortalFrame(level, pos);
    }
}
