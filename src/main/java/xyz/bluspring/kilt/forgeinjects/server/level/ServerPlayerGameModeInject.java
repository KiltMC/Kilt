// TRACKED HASH: 6b790991592a47c6c6a8e8be25116c80e58fd91d
package xyz.bluspring.kilt.forgeinjects.server.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
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