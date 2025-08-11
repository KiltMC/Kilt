package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(DetectorRailBlock.class)
public abstract class DetectorRailBlockInject extends BaseRailBlockInject {
    @Shadow @Final public static EnumProperty<RailShape> SHAPE;
    @Unique private Runnable kilt$defaultState;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DetectorRailBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void kilt$storeDefaultState(DetectorRailBlock instance, BlockState blockState, Operation<Void> original) {
        this.kilt$defaultState = () -> original.call(instance, blockState);
        this.registerDefaultState();
    }

    protected void registerDefaultState() {
        if (this.kilt$defaultState != null)
            this.kilt$defaultState.run();

        this.kilt$defaultState = null;
    }

    @WrapOperation(method = "getAnalogOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DetectorRailBlock;getInteractingMinecartOfType(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/lang/Class;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private <T> List<T> kilt$tryGetForgeComparatorLevels(DetectorRailBlock instance, Level level, BlockPos pos, Class<T> cartType, Predicate<Entity> filter, Operation<List<T>> original, @Cancellable CallbackInfoReturnable<Integer> cir) {
        List<AbstractMinecart> carts = (List<AbstractMinecart>) original.call(instance, level, pos, cartType, (Predicate<Entity>) Entity::isAlive);

        if (!carts.isEmpty() && carts.get(0).getComparatorLevel() > -1) {
            cir.setReturnValue(carts.get(0).getComparatorLevel());
            return List.of();
        }

        return (List<T>) carts.stream().filter(filter).collect(Collectors.toList());
    }

    @ModifyArg(method = "createBlockStateDefinition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/StateDefinition$Builder;add([Lnet/minecraft/world/level/block/state/properties/Property;)Lnet/minecraft/world/level/block/state/StateDefinition$Builder;"))
    private Property<?>[] kilt$useCustomShapeProperty(Property<?>[] properties) {
        var original = properties[0];

        if (original == SHAPE) // Kilt: mod compat :D
            properties[0] = this.getShapeProperty();

        return properties;
    }
}
