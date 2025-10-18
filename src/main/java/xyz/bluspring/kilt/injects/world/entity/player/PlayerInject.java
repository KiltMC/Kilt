// TRACKED HASH: da42f0fcd542552388a5aff060abf470c54f9f10
package xyz.bluspring.kilt.injects.world.entity.player;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.event.EventHooks;
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
import xyz.bluspring.kilt.injections.world.entity.PlayerInjection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(Player.class)
public abstract class PlayerInject extends LivingEntity implements IPlayerExtension, PlayerInjection {
    @Shadow public abstract float getDestroySpeed(BlockState state);
    @Shadow @Final private Inventory inventory;
    @Shadow @Final private Abilities abilities;
    @Shadow public abstract void resetAttackStrengthTicker();

    @CreateStatic
    private static final String PERSISTED_NBT_TAG = PlayerInjection.PERSISTED_NBT_TAG;

    @Unique private final Collection<MutableComponent> prefixes = new LinkedList<>();
    @Unique private final Collection<MutableComponent> suffixes = new LinkedList<>();
    @Unique @Nullable private Pose forcedPose;
    @Unique private long lastDayTimeTick = -1L;

    protected PlayerInject(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder kilt$appendNeoCreativeFlightAttribute(AttributeSupplier.Builder original) {
        return original
            .add(NeoForgeMod.CREATIVE_FLIGHT);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void kilt$playerTickStart(CallbackInfo ci) {
        EventHooks.firePlayerTickPre((Player) (Object) this);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z", ordinal = 0))
    private boolean kilt$checkShouldEntityContinueSleeping(boolean original) {
        return !EventHooks.canEntityContinueSleeping(this, original ? Player.BedSleepingProblem.NOT_POSSIBLE_NOW : null);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"))
    private void kilt$advanceRestStatIfCorrectDaytimeAdvance(Player instance, ResourceLocation resourceLocation, Operation<Void> original) {
        if (this.level().getDayTimeFraction() < 0 || this.level().getDayTimeFraction() >= 1 || this.lastDayTimeTick != this.level().getDayTime() || !this.level().getGameRules().getRule(GameRules.RULE_DAYLIGHT).get()) {
            this.lastDayTimeTick = this.level().getDayTime();
            original.call(instance, resourceLocation);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void kilt$playerTickEnd(CallbackInfo ci) {
        EventHooks.firePlayerTickPost((Player) (Object) this);
    }

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void kilt$useForcedPose(CallbackInfo ci) {
        if (forcedPose != null) {
            this.setPose(forcedPose);
            ci.cancel();
        }
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void kilt$checkShouldDie(DamageSource damageSource, CallbackInfo ci) {
        if (CommonHooks.onLivingDeath(this, damageSource)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity kilt$checkPlayerTossEvent(Player instance, ItemStack itemStack, boolean dropAround, boolean includeName, Operation<ItemEntity> original) {
        return CommonHooks.kilt$onPlayerTossEvent(instance, () -> original.call(instance, itemStack, dropAround, includeName));
    }

    private final AtomicReference<BlockPos> kilt$dugBlockPos = new AtomicReference<>();

    @Override
    public float getDigSpeed(BlockState blockState, @Nullable BlockPos blockPos) {
        if (blockPos != null)
            this.kilt$dugBlockPos.set(blockPos);
        return this.getDestroySpeed(blockState);
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "getDestroySpeed")
    public float kilt$modifyBreakSpeed(float original, @Local(argsOnly = true) BlockState state) {
        var blockPos = this.kilt$dugBlockPos.getAndSet(null);

        if (blockPos != null)
            return EventHooks.getBreakSpeed((Player) (Object) this, state, original, blockPos);
        return original;
    }

    @Override
    public boolean hasCorrectToolForDrops(BlockState state, Level level, BlockPos pos) {
        return EventHooks.doPlayerHarvestCheck((Player) (Object) this, state, level, pos);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void kilt$storeOriginalAmount(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("originalAmount") LocalFloatRef amountRef) {
        amountRef.set(amount);
    }

    @Definition(id = "amount", local = @Local(type = float.class, argsOnly = true))
    @Expression("amount == 0.0")
    @Inject(method = "hurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$modifyAccountToScale(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local LocalFloatRef amountRef, @Share("originalAmount") LocalFloatRef originalAmountRef) {
        var scalingFunction = source.type().scaling().getScalingFunction();

        // Just handle it normally.
        if (scalingFunction == IScalingFunction.DEFAULT)
            return;

        var scaled = scalingFunction.scaleDamage(source, (Player) (Object) this, originalAmountRef.get(), this.level().getDifficulty());
        amountRef.set(scaled);
    }

    @Definition(id = "useItem", field = "Lnet/minecraft/world/entity/player/Player;useItem:Lnet/minecraft/world/item/ItemStack;")
    @Definition(id = "is", method = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    @Definition(id = "SHIELD", field = "Lnet/minecraft/world/item/Items;SHIELD:Lnet/minecraft/world/item/Item;")
    @Expression("this.useItem.is(SHIELD)")
    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanBeShield(boolean original) {
        return original || this.useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPlayerAttack(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!CommonHooks.onPlayerAttack((Player) (Object) this, source, amount))
            cir.setReturnValue(false);
    }

    @Inject(method = "method_20266", at = @At("TAIL"))
    private static void kilt$callPlayerDestroyItem(InteractionHand interactionHand, Player player, CallbackInfo ci) {
        EventHooks.onPlayerDestroyItem(player, player.getUseItem(), interactionHand);
        player.stopUsingItem();
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInvulnerableTo(Lnet/minecraft/world/damagesource/DamageSource;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$callLivingHurt(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef amountRef) {
        amountRef.set(CommonHooks.onLivingHurt(this, damageSource, damageAmount));

        if (amountRef.get() <= 0)
            ci.cancel();
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setAbsorptionAmount(F)V", shift = At.Shift.AFTER))
    private void kilt$callLivingDamage(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Local(ordinal = 1) LocalFloatRef damageRef) {
        damageRef.set(CommonHooks.onLivingDamage(this, damageSource, damageRef.get()));
    }

    @Inject(method = "interactOn", at = @At(value = "RETURN", ordinal = 1))
    private void kilt$callPlayerDestroyItem(Entity entityToInteractOn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack stack2) {
        if (!this.abilities.instabuild && stack.isEmpty()) {
            EventHooks.onPlayerDestroyItem((Player) (Object) this, stack2, hand);
        }
    }

    @Inject(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void kilt$callPlayerDestroyItem(Entity entityToInteractOn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local(ordinal = 1) ItemStack stack) {
        EventHooks.onPlayerDestroyItem((Player) (Object) this, stack, hand);
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPlayerTargetAttack(Entity target, CallbackInfo ci) {
        if (!CommonHooks.onPlayerAttackTarget((Player) (Object) this, target))
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
        EventHooks.onPlayerDestroyItem((Player) (Object) this, copy.get(), InteractionHand.MAIN_HAND);
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
        EventHooks.onPlayerWakeup((Player) (Object) this, wakeImmediately, updateLevelForSleepingPlayers);
    }

    @WrapOperation(method = "findRespawnPositionAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;"))
    private static <T> Optional<T> kilt$tryGetRespawnPosition(Operation<Optional<T>> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) float orientation) {
        var state = level.getBlockState(pos);
        var respawnPos = state.getRespawnPosition(EntityType.PLAYER, level, pos, orientation, null);

        if (respawnPos.isEmpty())
            return original.call();
        return (Optional<T>) respawnPos;
    }

    @Inject(method = "causeFallDamage", at = @At(value = "RETURN", ordinal = 0))
    private void kilt$onPlayerFallEvent(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        EventHooks.onPlayerFall((Player) (Object) this, fallDistance, fallDistance);
    }

    /*@ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder kilt$addForgeAttributes(AttributeSupplier.Builder original) {
        return original
            .add(ForgeMod.BLOCK_REACH.get())
            .add(Attributes.ATTACK_KNOCKBACK)
            .add(ForgeMod.ENTITY_REACH.get());
    }*/

    @Unique private final LazyOptional<IItemHandler> playerMainHandler = LazyOptional.of(() -> new PlayerMainInvWrapper(inventory));
    @Unique private final LazyOptional<IItemHandler> playerEquipmentHandler = LazyOptional.of(() -> new CombinedInvWrapper(new PlayerArmorInvWrapper(inventory), new PlayerOffhandInvWrapper(inventory)));
    @Unique private final LazyOptional<IItemHandler> playerJoinedHandler = LazyOptional.of(() -> new PlayerInvWrapper(inventory));

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