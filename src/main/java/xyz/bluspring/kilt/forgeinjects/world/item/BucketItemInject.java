package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBucketPickup;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.item.BucketItemInjection;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(BucketItem.class)
public abstract class BucketItemInject extends Item implements BucketItemInjection {
    @Mutable
    @Shadow @Final private Fluid content;
    private final Supplier<? extends Fluid> fluidSupplier;

    @Override
    public Fluid getFluid() {
        if (fluidSupplier == null)
            return this.content;
        else
            return fluidSupplier.get();
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;", shift = At.Shift.BEFORE, ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$callBucketUseEvent(Level level, Player player, InteractionHand usedHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack itemStack, BlockHitResult blockHitResult) {
        var ret = ForgeEventFactory.onBucketUse(player, level, itemStack, blockHitResult);
        if (ret != null)
            cir.setReturnValue(ret);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BucketPickup;getPickupSound()Ljava/util/Optional;"))
    public Optional<SoundEvent> kilt$checkPickupSoundBasedOnBlockState(BucketPickup instance, @Local BlockState blockState) {
        return ((IForgeBucketPickup) instance).getPickupSound(blockState);
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

    @CreateInitializer
    public BucketItemInject(Supplier<Fluid> fluidSupplier, Properties properties) {
        super(properties);

        this.content = null;
        this.fluidSupplier = fluidSupplier;
    }
}
