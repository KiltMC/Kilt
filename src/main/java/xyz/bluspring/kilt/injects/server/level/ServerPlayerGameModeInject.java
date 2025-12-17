// TRACKED HASH: 6b790991592a47c6c6a8e8be25116c80e58fd91d
package xyz.bluspring.kilt.injects.server.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeInject {

	@Shadow
	private GameType gameModeForPlayer;

	@Shadow
	@Final
	protected ServerPlayer player;

	@Shadow
	protected ServerLevel level;

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