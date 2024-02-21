package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.LevelCapabilityProviderImpl;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;

import java.util.ArrayList;
import java.util.EnumSet;

@Mixin(Level.class)
@Extends(CapabilityProvider.class)
public abstract class LevelInject implements CapabilityProviderInjection, LevelCapabilityProviderImpl, IForgeLevel, LevelInjection {
    @Shadow @Final public boolean isClientSide;

    @Shadow public abstract ResourceKey<Level> dimension();

    @Shadow public abstract BlockState getBlockState(BlockPos blockPos);

    @Shadow public abstract void updateNeighbourForOutputSignal(BlockPos pos, Block block);

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
