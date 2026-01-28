// TRACKED HASH: 9d42bfbad9755a8542e0412d358b0c5c2d30ee69
package xyz.bluspring.kilt.injects.world.level.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.LiquidBlockInjection;

import java.util.List;
import java.util.function.Supplier;

@Mixin(value = LiquidBlock.class, priority = 1070)
@Implements(@Interface(iface = LiquidBlockInjection.class, prefix = "kilt$i$"))
public abstract class LiquidBlockInject extends Block {
    @Shadow @Final @Mutable
    protected FlowingFluid fluid;

    public LiquidBlockInject(Properties properties) {
        super(properties);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlock;shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), method = "neighborChanged")
    public boolean kilt$useForgeNeighborsChanged(LiquidBlock instance, Level level, BlockPos blockPos, BlockState blockState) {
        return !FluidInteractionRegistry.canInteract(level, blockPos);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlock;shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), method = "onPlace")
    public boolean kilt$useForgeOnPlace(LiquidBlock instance, Level level, BlockPos blockPos, BlockState blockState) {
        return !FluidInteractionRegistry.canInteract(level, blockPos);
    }
}