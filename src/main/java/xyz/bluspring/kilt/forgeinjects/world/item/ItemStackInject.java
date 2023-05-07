package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.ItemStackCapabilityProviderImpl;
import xyz.bluspring.kilt.injections.item.ItemStackInjection;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

import java.util.function.Supplier;

@Mixin(ItemStack.class)
public abstract class ItemStackInject implements IForgeItemStack, CapabilityProviderInjection, ItemStackCapabilityProviderImpl, ItemStackInjection {
    private CompoundTag capNBT;

    @Override
    public CompoundTag getCapNBT() {
        return capNBT;
    }

    @Shadow public abstract void setTag(@Nullable CompoundTag compoundTag);

    private final CapabilityProviderWorkaround<ItemStack> workaround = new CapabilityProviderWorkaround<>(ItemStack.class, (ItemStack) (Object) this);

    @Override
    public CapabilityProviderWorkaround<ItemStack> getWorkaround() {
        return workaround;
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V")
    public void kilt$registerCapabilities(CompoundTag compoundTag, CallbackInfo ci) {
        this.capNBT = compoundTag.contains("ForgeCaps") ? compoundTag.getCompound("ForgeCaps") : null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return workaround.getCapability(cap, side);
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
    public boolean areCapsCompatible(CapabilityProvider<ItemStack> other) {
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
    public void deserializeNBT(CompoundTag nbt) {
        var itemStack = ItemStack.of(nbt);
        this.setTag(nbt);

        if (itemStack.getCapNBT() != null)
            deserializeCaps(itemStack.getCapNBT());
    }
}
