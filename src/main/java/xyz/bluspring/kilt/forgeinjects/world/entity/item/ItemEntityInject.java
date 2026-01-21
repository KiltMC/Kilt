package xyz.bluspring.kilt.forgeinjects.world.entity.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.forgeinjects.world.entity.EntityInject;
import xyz.bluspring.kilt.injections.world.entity.item.ItemEntityInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(ItemEntity.class)
public abstract class ItemEntityInject extends EntityInject implements ItemEntityInjection {
    @Shadow @Final private static int LIFETIME;

    @Shadow public abstract ItemStack getItem();

    @Shadow private int pickupDelay;
    public int lifespan = LIFETIME;

    @Override
    public int kilt$getLifespan() {
        return lifespan;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;DDD)V", at = @At("TAIL"))
    private void kilt$initLifespan(Level level, double posX, double posY, double posZ, ItemStack stack, double deltaX, double deltaY, double deltaZ, CallbackInfo ci) {
        this.lifespan = (stack.getItem() == null ? LIFETIME : stack.getEntityLifespan(level));
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/item/ItemEntity;)V", at = @At("TAIL"))
    private void kilt$initLifespanFromOther(ItemEntity other, CallbackInfo ci) {
        this.lifespan = ((ItemEntityInjection) other).kilt$getLifespan();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void kilt$checkEntityItemUpdate(CallbackInfo ci) {
        if (this.getItem().onEntityItemUpdate((ItemEntity) (Object) this))
            ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;isInWater()Z"))
    private void kilt$handleFluidCollision(CallbackInfo ci, @Local float f, @Share("hasHandledFluid") LocalBooleanRef hasHandledFluid) {
        var fluidType = this.getMaxHeightFluidType();
        if (!fluidType.isAir() && !fluidType.isVanilla() && this.getFluidTypeHeight(fluidType) > f) {
            fluidType.setItemMovement((ItemEntity) (Object) this);
            hasHandledFluid.set(true);
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;isNoGravity()Z", ordinal = 0))
    private boolean kilt$cancelIfHasHandledFluid(ItemEntity instance, Operation<Boolean> original, @Share("hasHandledFluid") LocalBooleanRef hasHandledFluid) {
        if (hasHandledFluid.get())
            return true;

        return original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0))
    private BlockState kilt$storeBlockState(Level instance, BlockPos pos, Operation<BlockState> original, @Share("groundPos") LocalRef<BlockPos> groundPosRef, @Share("blockState") LocalRef<BlockState> stateRef) {
        var state = original.call(instance, pos);
        groundPosRef.set(pos);
        stateRef.set(state);
        return state;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float kilt$handleWithForgeFriction(Block instance, Operation<Float> original, @Share("groundPos") LocalRef<BlockPos> groundPosRef, @Share("blockState") LocalRef<BlockState> stateRef) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Block.class, "getFriction", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return stateRef.get().getFriction(this.level(), groundPosRef.get(), (ItemEntity) (Object) this);
        }

        return original.call(instance);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=6000"))
    private int kilt$useForgeLifespan(int original) {
        if (original != LIFETIME) {
            return original;
        }

        return lifespan;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", ordinal = 1))
    private void kilt$checkItemExpireEvent(ItemEntity instance, Operation<Void> original) {
        int hook = ForgeEventFactory.onItemExpire(instance, this.getItem());
        if (hook < 0) {
            original.call(instance);
        } else {
            this.lifespan += hook;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kilt$discardIfEmpty(CallbackInfo ci) {
        if (this.getItem().isEmpty() && !this.isRemoved()) {
            this.discard();
        }
    }

    @Inject(method = "areMergable", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private static void kilt$checkCapsCompatible(ItemStack destinationStack, ItemStack originStack, CallbackInfoReturnable<Boolean> cir) {
        if (!destinationStack.areCapsCompatible((CapabilityProvider<ItemStack>) (Object) originStack)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;)V"))
    private void kilt$callForgeDestroyedIfPossible(ItemStack instance, ItemEntity itemEntity, Operation<Void> original, @Local(argsOnly = true) DamageSource source) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), Item.class, "onDestroyed", ItemEntity.class, DamageSource.class)) {
            instance.onDestroyed(itemEntity, source);
        } else {
            original.call(instance, itemEntity);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void kilt$saveLifespanData(CompoundTag compound, CallbackInfo ci) {
        compound.putInt("Lifespan", this.lifespan);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void kilt$loadLifespanData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("Lifespan")) {
            this.lifespan = compound.getInt("Lifespan");
        }
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;getItem()Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
    private void kilt$checkPickupDelay(Player player, CallbackInfo ci) {
        if (this.pickupDelay > 0)
            ci.cancel();
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void kilt$checkPickupEvent(Player player, CallbackInfo ci, @Share("copy") LocalRef<ItemStack> copy, @Local ItemStack stack, @Share("hook") LocalIntRef hookRef) {
        var hook = ForgeEventFactory.onItemPickup((ItemEntity) (Object) this, player);
        if (hook < 0) {
            ci.cancel();
            return;
        }

        hookRef.set(hook);
        copy.set(stack.copy());
    }

    @WrapOperation(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 0))
    private boolean kilt$checkHookOrAdd(Inventory instance, ItemStack stack, Operation<Boolean> original, @Share("hook") LocalIntRef hookRef, @Local(ordinal = 0) int i) {
        return hookRef.get() == 1 || i <= 0 || original.call(instance, stack);
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void kilt$callPlayerItemTouchEvent(Player player, CallbackInfo ci, @Local(ordinal = 0) LocalIntRef countRef, @Share("copy") LocalRef<ItemStack> copyRef, @Local ItemStack stack) {
        countRef.set(copyRef.get().getCount() - stack.getCount());
        copyRef.get().setCount(countRef.get());
        ForgeEventFactory.firePlayerItemPickupEvent(player, (ItemEntity) (Object) this, copyRef.get());
    }

    // TODO: implement changeDimension

    @ModifyExpressionValue(method = "makeFakeItem", at = @At(value = "CONSTANT", args = "intValue=5999"))
    private int kilt$useEntityLifespanIfPossible(int original) {
        var lifespan = this.getItem().getEntityLifespan(this.level()) - 1;

        if (lifespan == LIFETIME - 1) {
            return original;
        }

        return lifespan;
    }
}
