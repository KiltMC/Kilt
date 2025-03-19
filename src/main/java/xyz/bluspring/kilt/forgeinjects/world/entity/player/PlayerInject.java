// TRACKED HASH: da42f0fcd542552388a5aff060abf470c54f9f10
package xyz.bluspring.kilt.forgeinjects.world.entity.player;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.extensions.IForgePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.entity.PlayerInjection;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Player.class)
public abstract class PlayerInject extends LivingEntity implements IForgePlayer, PlayerInjection {
    @CreateStatic
    private static final String PERSISTED_NBT_TAG = PlayerInjection.PERSISTED_NBT_TAG;

    @Shadow public abstract float getDestroySpeed(BlockState state);

    @Shadow @Final private Inventory inventory;

    @Shadow @Final private Abilities abilities;

    @Shadow public abstract void resetAttackStrengthTicker();

    protected PlayerInject(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void kilt$playerTickStart(CallbackInfo ci) {
        ForgeEventFactory.onPlayerPreTick((Player) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void kilt$playerTickEnd(CallbackInfo ci) {
        ForgeEventFactory.onPlayerPostTick((Player) (Object) this);
    }

    private final AtomicReference<BlockPos> kilt$dugBlockPos = new AtomicReference<>();

    public float getDigSpeed(BlockState blockState, @Nullable BlockPos blockPos) {
        if (blockPos != null)
            this.kilt$dugBlockPos.set(blockPos);
        return this.getDestroySpeed(blockState);
    }

    @Inject(at = @At("TAIL"), method = "getDestroySpeed", cancellable = true)
    public void kilt$modifyBreakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        var blockPos = this.kilt$dugBlockPos.getAndSet(null);

        if (blockPos != null)
            cir.setReturnValue(ForgeEventFactory.getBreakSpeed((Player) (Object) this, state, cir.getReturnValue(), blockPos));
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPlayerAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!ForgeHooks.onPlayerAttack((Player) (Object) this, source, amount))
            cir.setReturnValue(false);
    }

    @Inject(method = "method_20266", at = @At("TAIL"))
    private static void kilt$callPlayerDestroyItem(InteractionHand interactionHand, Player player, CallbackInfo ci) {
        ForgeEventFactory.onPlayerDestroyItem(player, player.getUseItem(), interactionHand);
        player.stopUsingItem();
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$callLivingHurt(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef amountRef) {
        amountRef.set(ForgeHooks.onLivingHurt(this, damageSource, damageAmount));

        if (amountRef.get() <= 0)
            ci.cancel();
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setAbsorptionAmount(F)V", shift = At.Shift.AFTER))
    private void kilt$callLivingDamage(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Local(ordinal = 1) LocalFloatRef damageRef) {
        damageRef.set(ForgeHooks.onLivingDamage(this, damageSource, damageRef.get()));
    }

    @Inject(method = "interactOn", at = @At(value = "RETURN", ordinal = 1))
    private void kilt$callPlayerDestroyItem(Entity entityToInteractOn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack stack2) {
        if (!this.abilities.instabuild && stack.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem((Player) (Object) this, stack2, hand);
        }
    }

    @Inject(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void kilt$callPlayerDestroyItem(Entity entityToInteractOn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local(ordinal = 1) ItemStack stack) {
        ForgeEventFactory.onPlayerDestroyItem((Player) (Object) this, stack, hand);
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPlayerTargetAttack(Entity target, CallbackInfo ci) {
        if (!ForgeHooks.onPlayerAttackTarget((Player) (Object) this, target))
            ci.cancel();
    }

    // Kilt: Critical hit implemented by Porting Lib

    @WrapWithCondition(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
    private boolean kilt$avoidStrengthTickerReset(Player instance) {
        return false;
    }

    @Definition(id = "entity", local = @Local(type = Entity.class, argsOnly = true))
    @Definition(id = "EnderDragonPart", type = EnderDragonPart.class)
    @Expression("entity instanceof EnderDragonPart")
    @WrapOperation(method = "attack", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$trySetAttackedMultipartEntity(Object object, Operation<Boolean> original, @Local(ordinal = 1) LocalRef<Entity> entity) {
        if (original.call(object))
            return true;

        if (object instanceof PartEntity<?> partEntity) {
            entity.set(partEntity.getParent());
        }

        return false;
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtEnemy(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player;)V"))
    private void kilt$storeStackCopy(Entity target, CallbackInfo ci, @Share("copy") LocalRef<ItemStack> copy, @Local(ordinal = 0) ItemStack stack) {
        copy.set(stack.copy());
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void kilt$callPlayerDestroyItem(Entity target, CallbackInfo ci, @Share("copy") LocalRef<ItemStack> copy) {
        ForgeEventFactory.onPlayerDestroyItem((Player) (Object) this, copy.get(), InteractionHand.MAIN_HAND);
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void kilt$resetStrengthTicker(Entity target, CallbackInfo ci) {
        if (target.isAttackable() && !target.skipAttackInteraction(this)) {
            this.resetAttackStrengthTicker();
        }
    }

    @WrapOperation(method = "disableShield", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;SHIELD:Lnet/minecraft/world/item/Item;"))
    private Item kilt$useUseItemItem(Operation<Item> original) {
        var item = original.call();

        if (item == Items.SHIELD) {
            return this.getUseItem().getItem();
        }

        return item;
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void kilt$callPlayerWakeup(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        ForgeEventFactory.onPlayerWakeup((Player) (Object) this, wakeImmediately, updateLevelForSleepingPlayers);
    }

    @WrapOperation(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;"))
    private static <T> Optional<T> kilt$tryGetRespawnPosition(Operation<Optional<T>> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) float orientation) {
        var state = level.getBlockState(pos);
        var respawnPos = state.getRespawnPosition(EntityType.PLAYER, level, pos, orientation, null);

        if (respawnPos.isEmpty())
            return original.call();
        return (Optional<T>) respawnPos;
    }

    /*@ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder kilt$addForgeAttributes(AttributeSupplier.Builder original) {
        return original
            .add(ForgeMod.BLOCK_REACH.get())
            .add(Attributes.ATTACK_KNOCKBACK)
            .add(ForgeMod.ENTITY_REACH.get());
    }*/

    @Unique private Pose forcedPose = null;

    @Unique private final LazyOptional<IItemHandler> playerMainHandler = LazyOptional.of(() -> new PlayerMainInvWrapper(inventory));
    @Unique private final LazyOptional<IItemHandler> playerEquipmentHandler = LazyOptional.of(() -> new CombinedInvWrapper(new PlayerArmorInvWrapper(inventory), new PlayerOffhandInvWrapper(inventory)));
    @Unique private final LazyOptional<IItemHandler> playerJoinedHandler = LazyOptional.of(() -> new PlayerInvWrapper(inventory));

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void kilt$useForcedPose(CallbackInfo ci) {
        if (forcedPose != null) {
            this.setPose(forcedPose);
            ci.cancel();
        }
    }

    @Override
    public @Nullable Pose getForcedPose() {
        return forcedPose;
    }

    @Override
    public void setForcedPose(Pose forcedPose) {
        this.forcedPose = forcedPose;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && this.isAlive()) {
            if (side == null)
                return playerJoinedHandler.cast();
            else if (side.getAxis().isVertical())
                return playerMainHandler.cast();
            else if (side.getAxis().isHorizontal())
                return playerEquipmentHandler.cast();
        }

        return super.getCapability(cap, side);
    }
}