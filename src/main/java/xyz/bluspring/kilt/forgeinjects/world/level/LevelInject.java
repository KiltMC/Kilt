package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.LevelCapabilityProviderImpl;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Level.class)
public abstract class LevelInject implements CapabilityProviderInjection, LevelCapabilityProviderImpl, IForgeLevel, LevelInjection {
    @Shadow @Final public boolean isClientSide;

    @Shadow public abstract ResourceKey<Level> dimension();

    @Shadow public abstract BlockState getBlockState(BlockPos blockPos);

    private final CapabilityProviderWorkaround<Level> workaround = new CapabilityProviderWorkaround<>(Level.class);

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
}
