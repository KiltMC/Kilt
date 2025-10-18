package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(MinecartItem.class)
public abstract class MinecartItemInject {
    @Mixin(targets = "net.minecraft.world.item.MinecartItem$1")
    public abstract static class AnonymousDispenseBehaviorInject {
        @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 1))
        private Comparable kilt$tryUseRailDirection(BlockState instance, Property property, Operation<Comparable> original, @Local Level level, @Local BlockPos pos) {
            var block = (BaseRailBlock) instance.getBlock();

            if (KiltHelper.INSTANCE.hasMethodOverride(block.getClass(), BaseRailBlock.class, "getRailDirection", BlockState.class, BlockGetter.class, BlockPos.class, AbstractMinecart.class)) {
                return block.getRailDirection(instance, level, pos, (AbstractMinecart) null);
            }

            return original.call(instance, property);
        }
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 0))
    private Comparable kilt$tryUseRailDirection(BlockState instance, Property property, Operation<Comparable> original, @Local Level level, @Local BlockPos pos) {
        var block = (BaseRailBlock) instance.getBlock();

        if (KiltHelper.INSTANCE.hasMethodOverride(block.getClass(), BaseRailBlock.class, "getRailDirection", BlockState.class, BlockGetter.class, BlockPos.class, AbstractMinecart.class)) {
            return block.getRailDirection(instance, level, pos, (AbstractMinecart) null);
        }

        return original.call(instance, property);
    }
}
