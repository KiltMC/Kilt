package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.block.LiquidBlockInjection;

import java.util.List;
import java.util.function.Supplier;

@Mixin(LiquidBlock.class)
public class LiquidBlockInject implements LiquidBlockInjection {
    @Shadow @Final protected FlowingFluid fluid;

    @Shadow @Final private List<FluidState> stateCache;

    @Override
    public FlowingFluid getFluid() {
        return (FlowingFluid) this.supplier.get();
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/level/material/FlowingFluid;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V")
    public void kilt$setFluidSupplier(FlowingFluid flowingFluid, BlockBehaviour.Properties properties, CallbackInfo ci) {
        fluidStateCacheInitialized = true;
        supplier = ForgeRegistries.FLUIDS.getDelegateOrThrow(flowingFluid);
    }

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", shift = At.Shift.BEFORE), method = "getFluidState")
    public void kilt$loadFluidStateCaches(BlockState blockState, CallbackInfoReturnable<FluidState> cir) {
        if (!fluidStateCacheInitialized)
            initFluidStateCache();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlock;shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), method = "neighborChanged")
    public boolean kilt$useForgeNeighborsChanged(LiquidBlock instance, Level level, BlockPos blockPos, BlockState blockState) {
        return !FluidInteractionRegistry.canInteract(level, blockPos);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlock;shouldSpreadLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), method = "onPlace")
    public boolean kilt$useForgeOnPlace(LiquidBlock instance, Level level, BlockPos blockPos, BlockState blockState) {
        return !FluidInteractionRegistry.canInteract(level, blockPos);
    }

    private Supplier<? extends Fluid> supplier;

    private boolean fluidStateCacheInitialized = false;
    protected synchronized void initFluidStateCache() {
        if (!fluidStateCacheInitialized) {
            this.stateCache.add(this.getFluid().getSource(false));

            for (int i = 0; i < 8; ++i)
                this.stateCache.add(this.getFluid().getFlowing(8 - i, false));

            this.stateCache.add(getFluid().getFlowing(8, true));
            fluidStateCacheInitialized = true;
        }
    }
}
