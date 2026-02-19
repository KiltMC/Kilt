// TRACKED HASH: 6b790991592a47c6c6a8e8be25116c80e58fd91d
package xyz.bluspring.kilt.forgeinjects.server.level;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeInject {
	@Shadow private GameType gameModeForPlayer;
	@Shadow @Final protected ServerPlayer player;
	@Shadow protected ServerLevel level;

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
	private boolean kilt$tryHandleDestroyedByPlayer(ServerLevel instance, BlockPos blockPos, boolean b, Operation<Boolean> original, @Local BlockState state) {
		if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IForgeBlock.class, "onDestroyedByPlayer", BlockState.class, BlockPos.class, Player.class, boolean.class, FluidState.class)) {
			return state.onDestroyedByPlayer(instance, blockPos, this.player, b, this.level.getFluidState(blockPos));
		}

		return original.call(instance, blockPos, b);
	}

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean kilt$checkCanHarvestBlock(ServerPlayer instance, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
		if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IForgeBlock.class, "canHarvestBlock", BlockState.class, BlockGetter.class, BlockPos.class, Player.class)) {
			return state.canHarvestBlock(this.level, pos, instance);
		}

		return original.call(instance, state);
	}

	@Unique
	private boolean destroyBlock(BlockPos pos, boolean canHarvest) {
		BlockState state = this.level.getBlockState(pos);
		boolean removed = state.onDestroyedByPlayer(this.level, pos, this.player, canHarvest, this.level.getFluidState(pos));

		if (removed) {
			state.getBlock().destroy(this.level, pos, state);
		}

		return removed;
	}

	@Definition(id = "gameModeForPlayer", field = "Lnet/minecraft/server/level/ServerPlayerGameMode;gameModeForPlayer:Lnet/minecraft/world/level/GameType;")
	@Definition(id = "SPECTATOR", field = "Lnet/minecraft/world/level/GameType;SPECTATOR:Lnet/minecraft/world/level/GameType;")
	@Expression("this.gameModeForPlayer == SPECTATOR")
	@Inject(method = "useItemOn", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
	private void kilt$callRightClickBlockEvent(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef, @Local BlockPos pos) {
		eventRef.set(ForgeHooks.onRightClickBlock(player, hand, pos, hitResult));

		if (eventRef.get().isCanceled())
			cir.setReturnValue(eventRef.get().getCancellationResult());
	}

	@Definition(id = "player", local = @Local(type = ServerPlayer.class, argsOnly = true))
	@Definition(id = "getMainHandItem", method = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;")
	@Definition(id = "isEmpty", method = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
	@Expression("player.getMainHandItem().isEmpty()")
	@Inject(method = "useItemOn", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
	private void kilt$tryHandleItemFirstUseEvent(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		UseOnContext context = new UseOnContext(player, hand, hitResult);
		if (eventRef.get().getUseItem() != Event.Result.DENY) {
			InteractionResult result = stack.onItemUseFirst(context);
			if (result != InteractionResult.PASS) {
				cir.setReturnValue(result);
			}
		}
	}

	@ModifyVariable(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), ordinal = 1)
	private boolean kilt$handleSneakBypassUse(boolean original, @Local(argsOnly = true) ServerPlayer player, @Local(argsOnly = true) Level level, @Local BlockPos pos) {
		return original && !(player.getMainHandItem().doesSneakBypassUse(level, pos, player) && player.getOffhandItem().doesSneakBypassUse(level, pos, player));
	}

	@Definition(id = "bl2", local = @Local(type = boolean.class, ordinal = 1))
	@Expression("bl2 == 0")
	@ModifyExpressionValue(method = "useItemOn", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean kilt$checkCanUseBlock(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		return eventRef.get().getUseBlock() == Event.Result.ALLOW || (eventRef.get().getUseBlock() != Event.Result.DENY && original);
	}

	@Definition(id = "stack", local = @Local(type = ItemStack.class, ordinal = 0, argsOnly = true))
	@Definition(id = "isEmpty", method = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
	@Expression("stack.isEmpty() == 0")
	@ModifyExpressionValue(method = "useItemOn", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean kilt$checkCanUseItemOnBlock(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		return eventRef.get().getUseItem() == Event.Result.ALLOW || original;
	}

	@Definition(id = "player", local = @Local(type = ServerPlayer.class, argsOnly = true))
	@Definition(id = "getCooldowns", method = "Lnet/minecraft/server/level/ServerPlayer;getCooldowns()Lnet/minecraft/world/item/ItemCooldowns;")
	@Definition(id = "isOnCooldown", method = "Lnet/minecraft/world/item/ItemCooldowns;isOnCooldown(Lnet/minecraft/world/item/Item;)Z")
	@Expression("player.getCooldowns().isOnCooldown(?) == 0")
	@ModifyExpressionValue(method = "useItemOn", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean kilt$checkCanUseItemOnBlock2(boolean original, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		return eventRef.get().getUseItem() == Event.Result.ALLOW || original;
	}

	@Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z"), cancellable = true)
	private void kilt$cancelIfDeny(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir, @Share("event") LocalRef<PlayerInteractEvent.RightClickBlock> eventRef) {
		if (eventRef.get().getUseItem() == Event.Result.DENY)
			cir.setReturnValue(InteractionResult.PASS);
	}

	/*@WrapOperation(
			method = "destroyBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/Item;canAttackBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"
			)
	)
	private boolean kilt$canAttackBlock(
			Item instance, BlockState state, Level level, BlockPos pos, Player player, Operation<Boolean> original,
			@Cancellable CallbackInfoReturnable<Boolean> cir, @Share("exp") LocalIntRef exp
	) {
		int expLocal = ForgeHooks.kilt$onBlockBreakEvent(
				level, gameModeForPlayer, this.player, pos,
				() -> original.call(instance, state, level, pos, player)
		);
		if (expLocal == -1) {
			// Patch for porting lib still breaking block when event is cancelled with an empty main hand item.
			if (FabricLoader.getInstance().isModLoaded("porting_lib_base") && player.getMainHandItem().isEmpty()) {
				cir.setReturnValue(false);
			}
			return false;
		}
		exp.set(expLocal);
		return true;
	}

	@Inject(
			method = "destroyBlock",
			at = @At(
					value = "TAIL",
					shift = At.Shift.BEFORE // Necessary to capture variables.
			)
	)
	private void kilt$dropXP(
			BlockPos pos, CallbackInfoReturnable<Boolean> cir,
			@Local BlockState blockState,
			@Local(ordinal = 0) boolean removed, @Share("exp") LocalIntRef exp
	) {
		if (removed && exp.get() > 0) {
			blockState.getBlock().popExperience(level, pos, exp.get());
		}
	}*/

}