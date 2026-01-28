// TRACKED HASH: 645dd70b9be6b9d17ac805f69fccfcbf748b17c1
package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.neoforged.neoforge.common.extensions.IBaseRailBlockExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockInject implements IBaseRailBlockExtension {
    @Shadow @Final private boolean isStraight;

    @Shadow public abstract Property<RailShape> getShapeProperty();

    @WrapOperation(method = {"getShape"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private Comparable<?> kilt$useForgeRailDirection(BlockState instance, Property<?> property, Operation<Comparable<?>> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), BaseRailBlock.class, "getRailDirection", BlockState.class, BlockGetter.class, BlockPos.class, AbstractMinecart.class)) {
            return getRailDirection(instance, level, pos, (AbstractMinecart) null);
        }

        return original.call(instance, property);
    }

    @WrapOperation(method = {"neighborChanged", "onRemove"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 0))
    private Comparable<?> kilt$useForgeRailDirection(BlockState instance, Property<?> property, Operation<Comparable<?>> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true, ordinal = 0) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), BaseRailBlock.class, "getRailDirection", BlockState.class, BlockGetter.class, BlockPos.class, AbstractMinecart.class)) {
            return getRailDirection(instance, level, pos, (AbstractMinecart) null);
        }

        return original.call(instance, property);
    }

    @Override
    public boolean isFlexibleRail(BlockState state, BlockGetter level, BlockPos pos) {
        return !this.isStraight;
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter level, BlockPos pos, @Nullable AbstractMinecart cart) {
        return state.getValue(getShapeProperty());
    }
}