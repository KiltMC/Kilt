// TRACKED HASH: 6ba20a4d1fb477de46b83b886b9e52ed60ea145b
package xyz.bluspring.kilt.injects.world.level.block;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockInject {
    @WrapOperation(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/PortalShape;findEmptyPortalShape(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"))
    private Optional<PortalShape> kilt$trySpawnPortalEvent(LevelAccessor level, BlockPos bottomLeft, Direction.Axis axis, Operation<Optional<PortalShape>> original) {
        return EventHooks.onTrySpawnPortal(level, bottomLeft, original.call(level, bottomLeft, axis));
    }

    @WrapOperation(method = "isPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private static boolean kilt$checkIfPortalFrame(BlockState instance, Object block, Operation<Boolean> original, @Local(argsOnly = true, name = "level") Level level, @Local(name = "testPos") BlockPos.MutableBlockPos pos) {
        return original.call(instance, block) || instance.isPortalFrame(level, pos);
    }
}
