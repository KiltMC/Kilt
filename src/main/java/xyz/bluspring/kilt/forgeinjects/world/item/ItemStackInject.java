package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
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

    @Shadow @Final @Deprecated @Mutable
    private Item item;
    @Shadow private int count;

    @Shadow public abstract Item getItem();

    private final CapabilityProviderWorkaround<ItemStack> workaround = new CapabilityProviderWorkaround<>(ItemStack.class, (ItemStack) (Object) this);

    @Override
    public CapabilityProviderWorkaround<ItemStack> getWorkaround() {
        return workaround;
    }

    public ItemStackInject(ItemLike item, int count) {}

    @CreateInitializer
    public ItemStackInject(ItemLike item, int count, CompoundTag tag) {
        this(item, count);
        this.capNBT = tag;
        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V")
    public void kilt$registerCapabilities(CompoundTag compoundTag, CallbackInfo ci) {
        this.capNBT = compoundTag.contains("ForgeCaps") ? compoundTag.getCompound("ForgeCaps") : null;
        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V")
    public void kilt$initForgeItemStack(ItemLike itemLike, int i, CallbackInfo ci) {
        // this might run twice.
        // TODO: figure out how to avoid double-running
        this.forgeInit();
    }

    @Inject(at = @At("TAIL"), method = "save")
    public void kilt$saveForgeCaps(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        var capNbt = this.serializeCaps();
        if (capNbt != null && !capNbt.isEmpty()) {
            compoundTag.put("ForgeCaps", capNbt);
        }
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
    public boolean areCapsCompatible(ICapabilityProviderImpl<ItemStack> stack) {
        if (stack instanceof ItemStackCapabilityProviderImpl stackWorkaround)
            return workaround.areCapsCompatible(stackWorkaround.getWorkaround());
        else if (stack instanceof CapabilityProvider<ItemStack> provider)
            return workaround.areCapsCompatible(provider);
        else
            return false;
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

    private void forgeInit() {
        if (this.item != null) {
            this.gatherCapabilities(() -> this.item.initCapabilities((ItemStack) (Object) this, this.capNBT));
            if (this.capNBT != null)
                this.deserializeCaps(this.capNBT);
        }
    }
}
