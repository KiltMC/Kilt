package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.mixin.RailStateAccessor;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

@Mixin(RailState.class)
public abstract class RailStateInject {
    @Shadow @Final private BaseRailBlock block;
    @Shadow @Final private List<BlockPos> connections;
    @Unique private boolean canMakeSlopes;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private Comparable kilt$tryUseRailDirection(BlockState instance, Property property, Operation<Comparable> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.block.getClass(), BaseRailBlock.class, "getRailDirection", BlockState.class, BlockGetter.class, BlockPos.class, AbstractMinecart.class)) {
            return block.getRailDirection(instance, level, pos, (AbstractMinecart) null);
        }

        return original.call(instance, property);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;isStraight()Z"))
    private boolean kilt$tryCheckFlexibleRail(BaseRailBlock instance, Operation<Boolean> original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), BaseRailBlock.class, "isFlexibleRail", BlockState.class, BlockGetter.class, BlockPos.class)) {
            return !instance.isFlexibleRail(state, level, pos);
        }

        return original.call(instance);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$checkCanMakeSlopes(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        this.canMakeSlopes = this.block.canMakeSlopes(state, level, pos);
    }

    @Definition(id = "railShape", local = @Local(type = RailShape.class, ordinal = 0))
    @Definition(id = "NORTH_SOUTH", field = "Lnet/minecraft/world/level/block/state/properties/RailShape;NORTH_SOUTH:Lnet/minecraft/world/level/block/state/properties/RailShape;")
    @Expression("railShape == NORTH_SOUTH")
    @ModifyExpressionValue(method = "connectTo", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanMakeSlopes(boolean original) {
        return original && canMakeSlopes;
    }

    @Definition(id = "railShape", local = @Local(type = RailShape.class, ordinal = 0))
    @Definition(id = "EAST_WEST", field = "Lnet/minecraft/world/level/block/state/properties/RailShape;EAST_WEST:Lnet/minecraft/world/level/block/state/properties/RailShape;")
    @Expression("railShape == EAST_WEST")
    @ModifyExpressionValue(method = "connectTo", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanMakeSlopes2(boolean original) {
        return original && canMakeSlopes;
    }

    @Inject(method = "connectTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"), cancellable = true)
    private void kilt$checkIsValidRailShape(RailState state, CallbackInfo ci, @Local RailShape newShape) {
        if (!this.block.isValidRailShape(newShape)) {
            this.connections.remove(((RailStateAccessor) state).getPos());
            ci.cancel();
        }
    }

    @Definition(id = "railShape", local = @Local(type = RailShape.class, ordinal = 1))
    @Definition(id = "NORTH_SOUTH", field = "Lnet/minecraft/world/level/block/state/properties/RailShape;NORTH_SOUTH:Lnet/minecraft/world/level/block/state/properties/RailShape;")
    @Expression("railShape == NORTH_SOUTH")
    @ModifyExpressionValue(method = "place", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanMakeSlopes3(boolean original) {
        return original && canMakeSlopes;
    }

    @Definition(id = "railShape", local = @Local(type = RailShape.class, ordinal = 1))
    @Definition(id = "EAST_WEST", field = "Lnet/minecraft/world/level/block/state/properties/RailShape;EAST_WEST:Lnet/minecraft/world/level/block/state/properties/RailShape;")
    @Expression("railShape == EAST_WEST")
    @ModifyExpressionValue(method = "place", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkCanMakeSlopes4(boolean original) {
        return original && canMakeSlopes;
    }

    @Definition(id = "railShape", local = @Local(type = RailShape.class, ordinal = 1))
    @Expression("railShape == null")
    @ModifyExpressionValue(method = "place", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsValidRailShape(boolean original, @Local(ordinal = 1) RailShape shape) {
        return original || !this.block.isValidRailShape(shape);
    }
}
