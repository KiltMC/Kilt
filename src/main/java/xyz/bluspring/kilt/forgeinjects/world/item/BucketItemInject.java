package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.item.BucketItemInjection;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(BucketItem.class)
public abstract class BucketItemInject extends ItemInject implements BucketItemInjection {
    @Shadow @Final private Fluid content;
    private Supplier<? extends Fluid> fluidSupplier;

    private void kilt$setFluidSupplier(Supplier<? extends Fluid> supplier) {
        fluidSupplier = supplier;
    }

    @Override
    public Fluid getFluid() {
        if (fluidSupplier == null)
            return this.content;
        else
            return fluidSupplier.get();
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        // this.. sounds so stupid. what.
        if (((BucketItem) (Object) this).getClass() == BucketItem.class)
            return new FluidBucketWrapper(stack);
        else
            // i love mixin
            return super.initCapabilities(stack, nbt);
    }

    @Override
    public void initializeClient(Consumer consumer) {
        super.initializeClient(consumer);
    }
}
