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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
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

	// Kilt TODO: pretty much everything in-between

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