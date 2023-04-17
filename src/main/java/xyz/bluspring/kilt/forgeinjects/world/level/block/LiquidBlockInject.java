package xyz.bluspring.kilt.forgeinjects.world.level.block;

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
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.LiquidBlockInjection;

import java.util.List;
import java.util.function.Supplier;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockInject extends Block implements LiquidBlockInjection {
    @Shadow @Final @Mutable
    protected FlowingFluid fluid;

    @Mutable
    @Shadow @Final private List<FluidState> stateCache;

    public LiquidBlockInject(Properties properties) {
        super(properties);
    }

    @Override
    public FlowingFluid getFluid() {
        if (this.fluid == null)
            this.fluid = (FlowingFluid) this.supplier.get();

        return this.fluid;
    }

    // This isn't a part of Forge itself, but it needs to be done in order to
    // make sure the Vanilla checks are able to actually have fluid function properly.
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(at = @At("HEAD"), method = {"isPathfindable", "getPickupSound", "pickupBlock", "shouldSpreadLiquid", "onPlace", "skipRendering", "updateShape", "neighborChanged"})
    public void kilt$cacheFluidState(CallbackInfo ci) {
        this.getFluid();
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

    @CreateInitializer
    public LiquidBlockInject(Supplier<? extends FlowingFluid> fluidSupplier, BlockBehaviour.Properties properties) {
        super(properties);

        this.fluid = null;
        this.stateCache = Lists.newArrayList();
        this.registerDefaultState(this.stateDefinition.any().setValue(FlowingFluid.LEVEL, 0));

        this.supplier = fluidSupplier;
    }
}
