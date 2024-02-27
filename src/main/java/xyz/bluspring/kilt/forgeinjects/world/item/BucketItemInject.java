// TRACKED HASH: b37723b1659555bec4982842296934e1d854cb3e
package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBucketPickup;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.item.BucketItemInjection;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mixin(value = BucketItem.class, priority = 1070)
public abstract class BucketItemInject extends Item implements BucketItemInjection {
    @Mutable
    @Shadow @Final private Fluid content;

    @Shadow public abstract boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result);

    @Unique
    private final Supplier<? extends Fluid> fluidSupplier;

    @Intrinsic
    @Override
    public Fluid getFluid() {
        if (fluidSupplier != null && this.content == null) {
            this.content = fluidSupplier.get();
        }

        return this.content;
    }

    @Unique
    private final AtomicReference<ItemStack> kilt$container = new AtomicReference<>(null);

    @Intrinsic
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult hitResult, @Nullable ItemStack container) {
        this.kilt$container.set(container);
        boolean value = this.emptyContents(player, level, pos, hitResult);
        this.kilt$container.set(null);

        return value;
    }

    // This isn't a part of Forge itself, but it needs to be done in order to
    // make sure the Vanilla checks are able to actually have fluid function properly.
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = {"emptyContents", "use", "playEmptySound"}, at = @At("HEAD"))
    public void kilt$cacheContents(CallbackInfo ci) {
        this.getFluid();
    }

    @Inject(method = "emptyContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/LiquidBlockContainer;canPlaceLiquid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z", shift = At.Shift.AFTER), cancellable = true)
    public void kilt$loadContainedFluidStack(Player player, Level level, BlockPos pos, BlockHitResult result, CallbackInfoReturnable<Boolean> cir, @Local BlockState state, @Local Block block, @Local(ordinal = 0) boolean bl) {
        var containedFluidStack = Optional.ofNullable(this.kilt$container.get()).flatMap(FluidUtil::getFluidContained);
        // TODO: figure out how to capture bl2
        var bl2 = state.isAir() || bl || block instanceof LiquidBlockContainer && ((LiquidBlockContainer)block).canPlaceLiquid(level, pos, state, this.content);

        if (bl2 && containedFluidStack.isPresent() && this.content.getFluidType().isVaporizedOnPlacement(level, pos, containedFluidStack.get())) {
            this.content.getFluidType().onVaporize(player, level, pos, containedFluidStack.get());

            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "emptyContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
    public boolean kilt$useForgeEmptyContents(BucketItem instance, Player player, Level level, BlockPos pos, BlockHitResult result) {
        return this.emptyContents(player, level, pos, result, this.kilt$container.get());
    }

    @Redirect(method = "emptyContents", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/BucketItem;content:Lnet/minecraft/world/level/material/Fluid;", ordinal = 4))
    public Fluid kilt$checkIfCanPlaceLiquid(BucketItem instance, @Local Block block, @Local Level level, @Local BlockPos pos, @Local BlockState state) {
        if (((LiquidBlockContainer) block).canPlaceLiquid(level, pos, state, this.content))
            return Fluids.WATER;

        return this.content;
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