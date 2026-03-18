// TRACKED HASH: 6b790991592a47c6c6a8e8be25116c80e58fd91d
package xyz.bluspring.kilt.injects.server.level;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeInject {
	@Shadow private GameType gameModeForPlayer;
	@Shadow @Final protected ServerPlayer player;
	@Shadow protected ServerLevel level;

	@WrapOperation(method = "setGameModeForPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;updatePlayerAbilities(Lnet/minecraft/world/entity/player/Abilities;)V"))
	private void kilt$preserveFlyingAbility(GameType instance, Abilities abilities, Operation<Void> original) {
		boolean wasFlying = abilities.flying;
		original.call(instance, abilities);
		abilities.flying = wasFlying || abilities.flying;
	}

	// Kilt: onLeftClickBlock handled by Porting Lib

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canAttackBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"))
	private boolean kilt$handleFireBlockBreakEvent(Item instance, BlockState state, Level level, BlockPos pos, Player player, Operation<Boolean> original) {
		if (player instanceof ServerPlayer serverPlayer) {
			var event = CommonHooks.kilt$fireBlockBreak(level, this.gameModeForPlayer, serverPlayer, pos, state, stack -> original.call(instance, state, level, pos, player));
			return !event.isCanceled();
		}

		return original.call(instance, state, level, pos, player);
	}

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
	private boolean kilt$tryHandleDestroyedByPlayer(ServerLevel instance, BlockPos blockPos, boolean b, Operation<Boolean> original, @Local(ordinal = 1) BlockState state) {
		if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), Block.class, "onDestroyedByPlayer", BlockState.class, BlockPos.class, Player.class, boolean.class, FluidState.class)) {
			return state.onDestroyedByPlayer(instance, blockPos, this.player, b, this.level.getFluidState(blockPos));
		}

		return original.call(instance, blockPos, b);
	}

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean kilt$checkCanHarvestBlock(ServerPlayer instance, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
		if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), Block.class, "canHarvestBlock", BlockState.class, BlockGetter.class, BlockPos.class, Player.class)) {
			return state.canHarvestBlock(this.level, pos, instance);
		}

		return original.call(instance, state);
	}

	@Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private void kilt$storeAvailableStacks(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 1) ItemStack copied, @Share("stack") LocalRef<ItemStack> stackRef, @Share("copied") LocalRef<ItemStack> copiedRef) {
		stackRef.set(stack);
		copiedRef.set(copied);
	}

	@Inject(method = "destroyBlock", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V")))
	private void kilt$handlePlayerDestroyItem(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share("stack") LocalRef<ItemStack> stackRef, @Share("copied") LocalRef<ItemStack> copiedRef) {
		if (stackRef.get().isEmpty() && !copiedRef.get().isEmpty()) {
			EventHooks.onPlayerDestroyItem(this.player, copiedRef.get(), InteractionHand.MAIN_HAND);
		}
	}

	@Unique
	private boolean removeBlock(BlockPos pos, BlockState state, boolean canHarvest) {
		boolean removed = state.onDestroyedByPlayer(this.level, pos, this.player, canHarvest, this.level.getFluidState(pos));

		if (removed) {
			state.getBlock().destroy(this.level, pos, state);
		}

		return removed;
	}

	// Kilt: onItemRightClick handled by Porting Lib

	@Definition(id = "gameModeForPlayer", field = "Lnet/minecraft/server/level/ServerPlayerGameMode;gameModeForPlayer:Lnet/minecraft/world/level/GameType;")
	@Definition(id = "SPECTATOR", field = "Lnet/minecraft/world/level/GameType;SPECTATOR:Lnet/minecraft/world/level/GameType;")
	@Expression("this.gameModeForPlayer == SPECTATOR")
	@Inject(method = "useItemOn", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
	private void kilt$cancelRightClickEvent(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir, @Local BlockPos pos, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		var event = CommonHooks.onRightClickBlock(player, hand, pos, result);
		eventRef.set(event);

		if (event.isCanceled())
			cir.setReturnValue(event.getCancellationResult());
	}

	@Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
	private void kilt$checkCanEventRun(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		var context = new UseOnContext(player, hand, result);

		if (eventRef.get().getUseItem() != TriState.FALSE) {
			var useResult = stack.onItemUseFirst(context);

			if (useResult != InteractionResult.PASS) {
				cir.setReturnValue(useResult);
			}
		}
	}

	@WrapOperation(method = "useItemOn", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0),
		@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1)
	})
	private boolean kilt$checkSneakBypassUse(ItemStack instance, Operation<Boolean> original, @Local(argsOnly = true) ServerPlayer player, @Local BlockPos pos) {
		return original.call(instance) || instance.doesSneakBypassUse(player.level(), pos, player);
	}

	@Definition(id = "bl2", local = @Local(type = boolean.class, ordinal = 1))
	@Expression("bl2 != 0")
	@ModifyExpressionValue(method = "useItemOn", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean kilt$checkCanUse(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		return eventRef.get().getUseBlock() == TriState.TRUE || (eventRef.get().getUseBlock().isDefault() && original);
	}

	@ModifyExpressionValue(method = "useItemOn", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 2),
		@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;isOnCooldown(Lnet/minecraft/world/item/Item;)Z")
	})
	private boolean kilt$checkShouldAllowEvent(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		if (eventRef.get().getUseItem().isTrue())
			return false;

		return original;
	}

	@Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z"), cancellable = true)
	private void kilt$checkShouldDenyEvent(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		if (eventRef.get().getUseItem().isFalse())
			cir.setReturnValue(InteractionResult.PASS);
	}
}