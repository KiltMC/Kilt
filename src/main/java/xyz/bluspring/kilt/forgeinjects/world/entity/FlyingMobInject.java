package xyz.bluspring.kilt.forgeinjects.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(FlyingMob.class)
public abstract class FlyingMobInject extends Mob {
    protected FlyingMobInject(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$storeCurrentBlockState(Level instance, BlockPos pos, Operation<BlockState> original, @Share("blockState") LocalRef<BlockState> stateRef, @Share("blockPos") LocalRef<BlockPos> posRef) {
        var state = original.call(instance, pos);
        stateRef.set(state);
        posRef.set(pos);

        return state;
    }

    @WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float kilt$callForgeFrictionIfPossible(Block instance, Operation<Float> original, @Share("blockState") LocalRef<BlockState> stateRef, @Share("blockPos") LocalRef<BlockPos> posRef) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IForgeBlock.class, "getFriction", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getFriction(stateRef.get(), this.level(), posRef.get(), this);
        }

        return original.call(instance);
    }
}
