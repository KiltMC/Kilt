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
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.entity.player.PlayerInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Collection;
import java.util.LinkedList;
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

    @WrapOperation(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    private void kilt$checkHurtAndDestroyItem(ItemStack instance, int amount, LivingEntity entity, EquipmentSlot slot, Operation<Void> original, @Local InteractionHand hand) {
        var currentAmount = instance.getCount();
        original.call(instance, amount, entity, slot);

        if (currentAmount != instance.getCount()) {
            // Assume that it's been broken.
            EventHooks.onPlayerDestroyItem((Player) (Object) this, instance, hand);
            this.stopUsingItem(); // Neo fixes MC-168573 here.
        }
    }

    // Kilt: We're a completely different file from LivingEntity and I still hate this system.

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void kilt$storeOriginalDamage(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Share("originalDamage") LocalFloatRef originalDamage, @Local(argsOnly = true) LocalFloatRef damageRef) {
        originalDamage.set(damageAmount);
        damageRef.set(this.kilt$getDamageContainers().peek().getNewDamage()); // Kilt: just directly use ours, i guess.
    }

    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float kilt$tryReduceWithArmorAbsorb(Player instance, DamageSource damageSource, float damageAmount, Operation<Float> original) {
        DamageContainer container = this.kilt$getDamageContainers().peek();

        var reduced = original.call(instance, damageSource, container.getNewDamage());
        container.setReduction(DamageContainer.Reduction.ARMOR, container.getNewDamage() - reduced);

        return reduced;
    }

    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float kilt$tryReduceWithMagicAbsorb(Player instance, DamageSource damageSource, float damageAmount, Operation<Float> original) {
        return original.call(instance, damageSource, this.kilt$getDamageContainers().peek().getNewDamage());
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private void kilt$callLivingPreDamageEvent(DamageSource damageSource, float damageAmount, CallbackInfo ci, @Share("damage") LocalFloatRef damageRef) {
        damageRef.set(CommonHooks.onLivingDamagePre(this, this.kilt$getDamageContainers().peek()));
    }

    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float kilt$doAbsorptionModification(float a, float b) {
        return this.kilt$getDamageContainers().peek().getNewDamage();
    }

    @Definition(id = "damageAmount", local = @Local(type = float.class, ordinal = 0, argsOnly = true))
    @Definition(id = "f", local = @Local(type = float.class, ordinal = 1))
    @Expression("f - damageAmount")
    @ModifyExpressionValue(method = "actuallyHurt", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float kilt$useAbsorbedDamage(float original, @Share("damage") LocalFloatRef damageRef) {
        return Math.min(damageRef.get(), this.kilt$getDamageContainers().peek().getReduction(DamageContainer.Reduction.ABSORPTION));
    }

    @ModifyArg(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setAbsorptionAmount(F)V"))
    private float kilt$clampAbsorptionAmount(float absorptionAmount) {
        return Math.max(absorptionAmount, 0);
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER))
    private void kilt$callDamageTaken(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        this.onDamageTaken(this.kilt$getDamageContainers().peek());
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void kilt$callLivingPostDamageEvent(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        CommonHooks.onLivingDamagePost(this, this.kilt$getDamageContainers().peek());
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

    @Definition(id = "level", method = "Lnet/minecraft/world/entity/player/Player;level()Lnet/minecraft/world/level/Level;")
    @Definition(id = "ServerLevel", type = ServerLevel.class)
    @Expression("this.level() instanceof ServerLevel")
    @Inject(method = "attack", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$storeStackCopy(Entity target, CallbackInfo ci, @Share("copy") LocalRef<ItemStack> copy, @Local(ordinal = 0) ItemStack stack) {
        copy.set(stack.copy());
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void kilt$callPlayerDestroyItem(Entity target, CallbackInfo ci, @Share("copy") LocalRef<ItemStack> copy, @Local ItemStack stack) {
        EventHooks.onPlayerDestroyItem((Player) (Object) this, copy.get(), stack == this.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
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

    @ModifyExpressionValue(method = "causeFallDamage", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z", opcode = Opcodes.GETFIELD))
    private boolean kilt$checkCanFly(boolean original) {
        return original || this.mayFly();
    }

    @Inject(method = "causeFallDamage", at = @At(value = "RETURN", ordinal = 0))
    private void kilt$onPlayerFallEvent(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        EventHooks.onPlayerFall((Player) (Object) this, fallDistance, fallDistance);
    }

    @WrapOperation(method = "tryToStartFallFlying", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean kilt$checkCanElytraFly(ItemStack instance, Item item, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), Item.class, "canElytraFly", ItemStack.class, LivingEntity.class)) {
            return instance.canElytraFly(this);
        }

        return original.call(instance, item);
    }

    // TODO: step sounds

    @Inject(method = "giveExperiencePoints", at = @At("HEAD"), cancellable = true)
    private void kilt$checkXpChangeEvent(int xpPoints, CallbackInfo ci, @Local(argsOnly = true) LocalIntRef xpPointsRef) {
        PlayerXpEvent.XpChange event = NeoForge.EVENT_BUS.post(new PlayerXpEvent.XpChange((Player) (Object) this, xpPoints));

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        xpPointsRef.set(event.getAmount());
    }

    // TODO: how tf
//    @Definition(id = "experienceLevel", field = "Lnet/minecraft/world/entity/player/Player;experienceLevel:I")
//    @Expression("this.experienceLevel = this.experienceLevel - ?")
//    @Redirect(method = "onEnchantmentPerformed", at = @At("MIXINEXTRAS:EXPRESSION"))
//    private void kilt$giveNegativeExperienceLevels() {
//
//    }

    @Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void kilt$checkXpLevelChangeEvent(int xpLevels, CallbackInfo ci, @Local(argsOnly = true) LocalIntRef xplevelsRef) {
        PlayerXpEvent.LevelChange event = NeoForge.EVENT_BUS.post(new PlayerXpEvent.LevelChange((Player) (Object) this, xpLevels));

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        xplevelsRef.set(event.getLevels());
    }

    // TODO: display names, held projectiles and stuff

    @Override
    public @Nullable Pose getForcedPose() {
        return forcedPose;
    }

    @Override
    public void setForcedPose(Pose forcedPose) {
        this.forcedPose = forcedPose;
    }
}