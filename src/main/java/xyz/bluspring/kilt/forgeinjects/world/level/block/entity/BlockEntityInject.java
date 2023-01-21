package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

import java.util.function.Supplier;

@Mixin(BlockEntity.class)
public class BlockEntityInject implements IForgeBlockEntity, CapabilityProviderInjection, ICapabilityProviderImpl<BlockEntityInject> {
    private CapabilityProviderWorkaround<BlockEntityInject> workaround = new CapabilityProviderWorkaround<>((Class<BlockEntityInject>) (Object) BlockEntity.class);

    @Override
    public CapabilityProviderWorkaround<BlockEntityInject> getWorkaround() {
        return workaround;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return workaround.getCapability(cap, side);
    }

    @Override
    public CompoundTag getPersistentData() {
        return null;
    }

    @Override
    public void gatherCapabilities() {
        workaround.invokeGatherCapabilities();
    }

    @Override
    public void gatherCapabilities(@Nullable ICapabilityProvider parent) {
        workaround.invokeGatherCapabilities(parent);
    }

    @Override
    public void gatherCapabilities(@Nullable Supplier<ICapabilityProvider> parent) {
        workaround.invokeGatherCapabilities(parent);
    }

    @Nullable
    @Override
    public CapabilityDispatcher getCapabilities() {
        return workaround.invokeGetCapabilities();
    }

    @Nullable
    @Override
    public CompoundTag serializeCaps() {
        return workaround.invokeSerializeCaps();
    }

    @Override
    public void deserializeCaps(CompoundTag tag) {
        workaround.invokeDeserializeCaps(tag);
    }

    @Override
    public boolean areCapsCompatible(CapabilityProvider<BlockEntityInject> other) {
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
}
