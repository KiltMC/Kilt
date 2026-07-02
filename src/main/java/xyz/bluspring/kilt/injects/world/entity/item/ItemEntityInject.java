package xyz.bluspring.kilt.injects.world.entity.item;

import java.util.UUID;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.entity.item.ItemEntityInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ItemEntity.class)
public abstract class ItemEntityInject extends Entity implements ItemEntityInjection {
    public ItemEntityInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow @Final private static int LIFETIME;
    @Shadow public abstract ItemStack getItem();

    @Shadow private int pickupDelay;
    @Shadow
    @Nullable
    private UUID target;
    @Shadow
    private int age;
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
        this.lifespan = other.kilt$getLifespan();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void kilt$checkEntityItemUpdate(CallbackInfo ci) {
        if (this.getItem().onEntityItemUpdate((ItemEntity) (Object) this))
            ci.cancel();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;isInWater()Z"))
    private void kilt$handleFluidCollision(CallbackInfo ci, @Share("hasHandledFluid") LocalBooleanRef hasHandledFluid) {
        var fluidType = this.getMaxHeightFluidType();
        if (!fluidType.isAir() && !fluidType.isVanilla() && this.getFluidTypeHeight(fluidType) > 0.1f) {
            fluidType.setItemMovement((ItemEntity) (Object) this);
            hasHandledFluid.set(true);
        }
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;applyGravity()V", ordinal = 0))
    private boolean kilt$cancelIfHasHandledFluid(ItemEntity instance, @Share("hasHandledFluid") LocalBooleanRef hasHandledFluid) {
        return !hasHandledFluid.get();
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
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IBlockExtension.class, "getFriction", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return stateRef.get().getFriction(this.level(), groundPosRef.get(), (ItemEntity) (Object) this);
        }

        return original.call(instance);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=6000"))
    private int kilt$useNeoForgeLifespan(int original) {
        if (original != LIFETIME) {
            return original;
        }

        return lifespan;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", ordinal = 1))
    private void kilt$checkItemExpireEvent(ItemEntity instance, Operation<Void> original) {
        this.lifespan = Mth.clamp(this.lifespan + EventHooks.onItemExpire(instance), 0, Short.MAX_VALUE - 1);

        if (this.age >= this.lifespan) {
            original.call(instance);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kilt$discardIfEmpty(CallbackInfo ci) {
        if (this.getItem().isEmpty() && !this.isRemoved()) {
            this.discard();
        }
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;)V"))
    private void kilt$tryCallNeoDestroyed(ItemStack instance, ItemEntity itemEntity, Operation<Void> original, @Local(argsOnly = true) DamageSource source) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), IItemExtension.class, "onDestroyed", ItemStack.class, ItemEntity.class, DamageSource.class)) {
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

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void kilt$checkPickupEvent(Player player, CallbackInfo ci, @Share("result") LocalRef<TriState> resultRef, @Share("copy") LocalRef<ItemStack> copyRef, @Local ItemStack stack) {
        var result = EventHooks.fireItemPickupPre((ItemEntity) (Object) this, player).canPickup();
        if (result.isFalse()) {
            ci.cancel();
            return;
        }

        resultRef.set(result);
        copyRef.set(stack.copy());
    }

    @Definition(id = "pickupDelay", field = "Lnet/minecraft/world/entity/item/ItemEntity;pickupDelay:I")
    @Expression("this.pickupDelay == 0")
    @ModifyExpressionValue(method = "playerTouch", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkHookOrAdd(boolean original, @Share("result") LocalRef<TriState> resultRef) {
        return (resultRef.get() != null && resultRef.get().isTrue()) || original;
    }

    @ModifyVariable(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V", ordinal = 0, shift = At.Shift.BEFORE), ordinal = 0)
    private int kilt$callPlayerItemTouchEvent(int count, @Share("copy") LocalRef<ItemStack> copyRef, @Local ItemStack stack, @Local(argsOnly = true) Player player) {
        EventHooks.fireItemPickupPost((ItemEntity) (Object) this, player, copyRef.get());
        return copyRef.get().getCount() - stack.getCount();
    }

    @Override
    public @Nullable UUID getTarget() {
        return this.target;
    }

    @ModifyExpressionValue(method = "makeFakeItem", at = @At(value = "CONSTANT", args = "intValue=5999"))
    private int kilt$useEntityLifespanIfPossible(int original) {
        var lifespan = this.getItem().getEntityLifespan(this.level()) - 1;

        if (lifespan == LIFETIME - 1) {
            return original;
        }

        return lifespan;
    }
}
