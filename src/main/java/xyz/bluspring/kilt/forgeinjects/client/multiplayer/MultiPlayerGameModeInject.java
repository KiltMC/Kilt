package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeInject {
    @Shadow @Final private Minecraft minecraft;

    @Shadow private ItemStack destroyingItem;

    @Shadow private GameType localPlayerMode;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void kilt$callStartBreakEvent(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.player.getMainHandItem().onBlockStartBreak(pos, minecraft.player))
            cir.setReturnValue(false);
    }

    @WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 0))
    private boolean kilt$callDestroyedByPlayer(Level instance, BlockPos pos, BlockState newState, int flags, Operation<Boolean> original, @Local BlockState state, @Local FluidState fluidState) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IForgeBlock.class, "onDestroyedByPlayer", BlockState.class, Level.class, BlockPos.class, Player.class, boolean.class, FluidState.class)) {
            return state.onDestroyedByPlayer(instance, pos, minecraft.player, false, fluidState);
        }

        return original.call(instance, pos, newState, flags);
    }

    /*@WrapWithCondition(method = "method_41936", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$handleLeftClickBlock(MultiPlayerGameMode instance, BlockPos pos, @Local(argsOnly = true) Direction direction) {
        return !ForgeHooks.onLeftClickBlock(minecraft.player, pos, direction, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK).isCanceled();
    }*/

    @Unique private PlayerInteractEvent.LeftClickBlock kilt$leftClickBlock;

    // Kilt: handled via Architectury
    /*@Inject(method = "startDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 1))
    private void kilt$callForgeLeftClickBlockEvent(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
        this.kilt$leftClickBlock = ForgeHooks.onLeftClickBlock(this.minecraft.player, loc, face);
    }

    @WrapWithCondition(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;attack(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"))
    private boolean kilt$handleUseBlockEvent(BlockState instance, Level level, BlockPos blockPos, Player player) {
        return this.kilt$leftClickBlock.getUseBlock() != Event.Result.DENY;
    }

    @Inject(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"), cancellable = true)
    private void kilt$handleParticleEvent(BlockState blockState, BlockPos blockPos, Direction direction, int i, CallbackInfoReturnable<Packet> cir) {
        if (kilt$leftClickBlock.getUseItem() == Event.Result.DENY) {
            cir.setReturnValue(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
        }
    }

    @WrapMethod(method = "startDestroyBlock")
    private boolean kilt$resetLeftClickBlock(BlockPos loc, Direction face, Operation<Boolean> original) {
        var result = original.call(loc, face);
        this.kilt$leftClickBlock = null;
        return result;
    }*/

    @WrapWithCondition(method = "method_41935", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$checkCanDestroyBlock(MultiPlayerGameMode instance, BlockPos pos, @Local(argsOnly = true) Direction direction) {
        return !ForgeHooks.onLeftClickBlock(this.minecraft.player, pos, direction, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK).isCanceled();
    }

    @WrapOperation(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$handleGetSoundType(BlockState instance, Operation<SoundType> original, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), IForgeBlock.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getSoundType(this.minecraft.level, pos, this.minecraft.player);
        }

        return original.call(instance);
    }

    @Inject(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onDestroyBlock(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;F)V", ordinal = 1), cancellable = true)
    private void kilt$checkClientMineHold(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (ForgeHooks.onClientMineHold(this.minecraft.player, posBlock, directionFacing).getUseItem() == Event.Result.DENY)
            cir.setReturnValue(true);
    }

    // Kilt: block reach handled by other mods

    @ModifyReturnValue(method = "sameDestroyTarget", at = @At("RETURN"))
    private boolean kilt$checkShouldCauseBlockBreakReset(boolean original) {
        // Kilt: Force Fabric mods' behaviour
        if (this.destroyingItem.getItem().allowContinuingBlockBreaking(this.minecraft.player, this.destroyingItem, this.minecraft.player.getMainHandItem())) {
            return original;
        }

        return original && !this.destroyingItem.shouldCauseBlockBreakReset(this.minecraft.player.getMainHandItem());
    }

    @Definition(id = "SPECTATOR", field = "Lnet/minecraft/world/level/GameType;SPECTATOR:Lnet/minecraft/world/level/GameType;")
    @Definition(id = "localPlayerMode", field = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;localPlayerMode:Lnet/minecraft/world/level/GameType;")
    @Expression("this.localPlayerMode == SPECTATOR")
    @Inject(method = "performUseItemOn", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$callRightClickBlockEvent(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir, @Share("event") @NotNull LocalRef<PlayerInteractEvent.RightClickBlock> eventRef, @Local BlockPos pos) {
        eventRef.set(ForgeHooks.onRightClickBlock(player, hand, pos, result));

        if (eventRef.get().isCanceled())
            cir.setReturnValue(eventRef.get().getCancellationResult());
    }

    @Definition(id = "player", local = @Local(type = LocalPlayer.class, argsOnly = true))
    @Definition(id = "getMainHandItem", method = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;")
    @Definition(id = "isEmpty", method = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
    @Expression("player.getMainHandItem().isEmpty()")
    @Inject(method = "performUseItemOn", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$tryHandleItemFirstUseEvent(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef, @Local ItemStack stack) {
        UseOnContext context = new UseOnContext(player, hand, hitResult);
        if (eventRef.get().getUseItem() != Event.Result.DENY) {
            InteractionResult result = stack.onItemUseFirst(context);
            if (result != InteractionResult.PASS) {
                cir.setReturnValue(result);
            }
        }
    }

    @WrapOperation(method = "performUseItemOn", at = {
        @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0),
        @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1)
    })
    private boolean kilt$checkSneakBypassUse(ItemStack instance, Operation<Boolean> original, @Local(argsOnly = true) LocalPlayer player, @Local BlockPos pos) {
        return original.call(instance) || instance.doesSneakBypassUse(player.level(), pos, player);
    }

    @Definition(id = "bl2", local = @Local(type = boolean.class, ordinal = 1))
    @Expression("bl2 == 0")
    @ModifyExpressionValue(method = "performUseItemOn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanUseBlock(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
        return eventRef.get().getUseBlock() == Event.Result.ALLOW || (eventRef.get().getUseBlock() != Event.Result.DENY && original);
    }

    @Definition(id = "stack", local = @Local(type = ItemStack.class, ordinal = 0))
    @Definition(id = "isEmpty", method = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
    @Expression("stack.isEmpty() == 0")
    @ModifyExpressionValue(method = "performUseItemOn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanUseItemOnBlock(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
        return eventRef.get().getUseItem() == Event.Result.ALLOW || original;
    }

    @Definition(id = "player", local = @Local(type = LocalPlayer.class, argsOnly = true))
    @Definition(id = "getCooldowns", method = "Lnet/minecraft/client/player/LocalPlayer;getCooldowns()Lnet/minecraft/world/item/ItemCooldowns;")
    @Definition(id = "isOnCooldown", method = "Lnet/minecraft/world/item/ItemCooldowns;isOnCooldown(Lnet/minecraft/world/item/Item;)Z")
    @Expression("player.getCooldowns().isOnCooldown(?) == 0")
    @ModifyExpressionValue(method = "performUseItemOn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanUseItemOnBlock2(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
        return eventRef.get().getUseItem() == Event.Result.ALLOW || original;
    }

    @Inject(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isCreative()Z"), cancellable = true)
    private void kilt$cancelIfDeny(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
        if (eventRef.get().getUseItem() == Event.Result.DENY)
            cir.setReturnValue(InteractionResult.PASS);
    }

    @Inject(method = "method_41929", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void kilt$handleForgeDestroyItemEvent(InteractionHand interactionHand, Player player, MutableObject mutableObject, int i, CallbackInfoReturnable<Packet> cir, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack stack2) {
        if (stack2.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(player, stack, interactionHand);
        }
    }
}
