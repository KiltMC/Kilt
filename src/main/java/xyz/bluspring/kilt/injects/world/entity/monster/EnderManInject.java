package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class EnderManInject extends Monster {
    protected EnderManInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // TODO: do we need to worry about data manager issues?

    @ModifyExpressionValue(method = "isLookingAtMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0))
    private boolean kilt$checkShouldSuppressAnger(boolean original, @Local(argsOnly = true) Player player, @Local ItemStack stack) {
        return original || CommonHooks.shouldSuppressEnderManAnger((EnderMan) (Object) this, player, stack);
    }

    @WrapOperation(method = "teleport(DDD)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;randomTeleport(DDDZ)Z"))
    private boolean kilt$callRandomTeleportEvent(EnderMan instance, double x, double y, double z, boolean broadcastTeleport, Operation<Boolean> original, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var event = EventHooks.onEnderTeleport(this, x, y, z);

        if (event.isCanceled()) {
            cir.setReturnValue(false);
            return false;
        }

        return original.call(instance, event.getTargetX(), event.getTargetY(), event.getTargetZ(), broadcastTeleport);
    }

    @Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanLeaveBlockGoal")
    public abstract static class EndermanLeaveBlockGoalInject extends Goal {
        @Shadow @Final private EnderMan enderman;

        @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
        private boolean kilt$checkMobGriefing(boolean original) {
            return original || EventHooks.canEntityGrief(this.enderman.level(), this.enderman);
        }

        @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan$EndermanLeaveBlockGoal;canPlaceBlock(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Z"))
        private boolean kilt$checkCanPlaceBlockEvent(boolean original, @Local Level level, @Local(ordinal = 1) BlockPos pos1) {
            return original && !EventHooks.onBlockPlace(this.enderman, BlockSnapshot.create(level.dimension(), level, pos1), Direction.UP);
        }

        @WrapOperation(method = "canPlaceBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isCollisionShapeFullBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
        private boolean kilt$checkEndermanPlaceTag(BlockState instance, BlockGetter blockGetter, BlockPos blockPos, Operation<Boolean> original) {
            return !instance.is(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST) && original.call(instance, blockGetter, blockPos);
        }
    }

    @Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
    public abstract static class EndermanTakeBlockGoalInject extends Goal {
        @Shadow @Final private EnderMan enderman;

        @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
        private boolean kilt$checkMobGriefing(boolean original) {
            return original || EventHooks.canEntityGrief(this.enderman.level(), this.enderman);
        }
    }
}
