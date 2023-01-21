package xyz.bluspring.kilt.forgeinjects.world.level.entity.living;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.function.BiPredicate;

@Mixin(LivingEntity.class)
public class LivingEntityInject implements IForgeLivingEntity {
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return null;
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void canUpdate(boolean value) {

    }

    @Override
    public @Nullable Collection<ItemEntity> captureDrops() {
        return null;
    }

    @Override
    public Collection<ItemEntity> captureDrops(@Nullable Collection<ItemEntity> captureDrops) {
        return null;
    }

    @Override
    public CompoundTag getPersistentData() {
        return null;
    }

    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return false;
    }

    @Override
    public boolean isAddedToWorld() {
        return false;
    }

    @Override
    public void onAddedToWorld() {

    }

    @Override
    public void onRemovedFromWorld() {

    }

    @Override
    public void revive() {

    }

    @Override
    public double getFluidTypeHeight(FluidType type) {
        return 0;
    }

    @Override
    public FluidType getMaxHeightFluidType() {
        return null;
    }

    @Override
    public boolean isInFluidType(BiPredicate<FluidType, Double> predicate, boolean forAllTypes) {
        return false;
    }

    @Override
    public boolean isInFluidType() {
        return false;
    }

    @Override
    public FluidType getEyeInFluidType() {
        return null;
    }
}
