// TRACKED HASH: 9d42bfbad9755a8542e0412d358b0c5c2d30ee69
package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.level.block.LiquidBlockInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

@Mixin(value = LiquidBlock.class, priority = 1070)
@Implements(@Interface(iface = LiquidBlockInjection.class, prefix = "kilt$i$"))
public abstract class LiquidBlockInject extends Block {
    @Shadow @Final @Mutable
    protected FlowingFluid fluid;

    public LiquidBlockInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlock;shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), method = {"neighborChanged", "onPlace"})
    public boolean kilt$checkHandleNeo(LiquidBlock instance, Level level, BlockPos pos, BlockState state, Operation<Boolean> original) {
        if (FluidInteractionRegistry.kilt$canInteract(level, pos, false))
            return false;

        return original.call(instance, level, pos, state);
    }

    @Intrinsic
    public FlowingFluid kilt$i$getFluid() {
        return fluid;
    }
}
