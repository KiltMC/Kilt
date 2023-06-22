package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.LevelCapabilityProviderImpl;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Supplier;

@Mixin(Level.class)
public abstract class LevelInject implements CapabilityProviderInjection, LevelCapabilityProviderImpl, IForgeLevel, LevelInjection {
    @Shadow @Final public boolean isClientSide;

    @Shadow public abstract ResourceKey<Level> dimension();

    @Shadow public abstract BlockState getBlockState(BlockPos blockPos);

    @Shadow public abstract void updateNeighbourForOutputSignal(BlockPos pos, Block block);

    private final CapabilityProviderWorkaround<Level> workaround = new CapabilityProviderWorkaround<>(Level.class, (Level) (Object) this);

    public CapabilityProviderWorkaround<Level> getWorkaround() {
        return workaround;
    }

    @Override
    public boolean areCapsCompatible(CapabilityProvider<Level> other) {
        return workaround.areCapsCompatible(other);
    }

    @Override
    public boolean areCapsCompatible(@Nullable CapabilityDispatcher other) {
        return workaround.areCapsCompatible(other);
    }

    @Override
    public void invalidateCaps() {
        workaround.invalidateCaps();
    }

    @Override
    public void reviveCaps() {
        workaround.reviveCaps();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return workaround.getCapability(cap, side);
    }

    private double maxEntityRadius = 2.0D;
    @Override
    public double getMaxEntityRadius() {
        return maxEntityRadius;
    }

    @Override
    public double increaseMaxEntityRadius(double value) {
        if (value > maxEntityRadius)
            maxEntityRadius = value;
        return maxEntityRadius;
    }

    public ArrayList<BlockSnapshot> capturedBlockSnapshots = new ArrayList<>();

    @Override
    public ArrayList<BlockSnapshot> getCapturedBlockSnapshots() {
        return capturedBlockSnapshots;
    }

    @Override
    public void gatherCapabilities(@Nullable Supplier<ICapabilityProvider> parent) {
        workaround.invokeGatherCapabilities(parent);
    }

    @Redirect(method = "getSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isRedstoneConductor(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    public boolean kilt$checkForWeakPower(BlockState instance, BlockGetter blockGetter, BlockPos blockPos, BlockPos unused, Direction facing) {
        return ((IForgeBlockState) instance).shouldCheckWeakPower((Level) blockGetter, blockPos, facing);
    }

    @Redirect(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    public boolean kilt$checkForNeighbourChange(BlockState instance, Block unused, BlockPos blockPos, @Local(index = 1) BlockPos directionPos) {
        ((IForgeBlockState) instance).onNeighborChange((Level) (Object) this, directionPos, blockPos);
        // Don't trigger the Vanilla neighbour change.
        return false;
    }

    @Redirect(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 1))
    public boolean kilt$getWeakChange(BlockState instance, Block unused, BlockPos blockPos, Block block, @Local(index = 1) BlockPos directionPos) {
        return ((IForgeBlockState) instance).getWeakChanges((Level) (Object) this, directionPos);
    }

    @Inject(method = "blockEntityChanged", at = @At("TAIL"))
    public void kilt$updateNeighbourOutputSignalsForChange(BlockPos pos, CallbackInfo ci) {
        this.updateNeighbourForOutputSignal(pos, this.getBlockState(pos).getBlock());
    }

    @Inject(method = "removeBlockEntity", at = @At("TAIL"))
    public void kilt$updateNeighbourOutputSignalsForRemoval(BlockPos pos, CallbackInfo ci) {
        this.updateNeighbourForOutputSignal(pos, this.getBlockState(pos).getBlock());
    }

    @Inject(method = "updateNeighborsAt", at = @At("TAIL"))
    public void kilt$notifyNeighbours(BlockPos pos, Block block, CallbackInfo ci) {
        // why is "isCanceled()" added at the end?
        ForgeEventFactory.onNeighborNotify((Level) (Object) this, pos, this.getBlockState(pos), EnumSet.allOf(Direction.class), false).isCanceled();
    }
}
