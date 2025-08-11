// TRACKED HASH: 8a81156928e5ec0380e740212b565fcc26b30d8b
package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.PoweredRailBlockInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(PoweredRailBlock.class)
public abstract class PoweredRailBlockInject extends BaseRailBlock implements PoweredRailBlockInjection {
    @Shadow public abstract Property<RailShape> getShapeProperty();

    @Shadow @Final public static BooleanProperty POWERED;
    @Shadow @Final public static EnumProperty<RailShape> SHAPE;
    private boolean isActivator = false;

    protected PoweredRailBlockInject(boolean bl, Properties properties) {
        super(bl, properties);
    }

    @Override
    public void registerDefaultState() {
        this.registerDefaultState(this.stateDefinition.any().setValue(PoweredRailBlock.SHAPE, RailShape.NORTH_SOUTH).setValue(PoweredRailBlock.POWERED, false).setValue(PoweredRailBlock.WATERLOGGED, false));
    }

    @Override
    public boolean isActivatorRail() {
        return isActivator;
    }

    @Override
    public void kilt$setActivator(boolean value) {
        isActivator = !value;
    }

    public PoweredRailBlockInject(Properties properties) {
        super(false, properties);
    }

    @CreateInitializer
    public PoweredRailBlockInject(Properties properties, boolean isPoweredRail) {
        this(properties);

        this.kilt$setActivator(isPoweredRail);
        this.registerDefaultState(); // oh fuck i forgot about this
    }

    @ModifyArg(method = "findPoweredRailSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 0))
    public Property<RailShape> kilt$useForgeShape(Property<RailShape> original) {
        if (original == SHAPE) {
            return this.getShapeProperty();
        }

        return original;
    }

    @WrapOperation(method = "isSameRailWithPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean kilt$checkIfBlockIsPoweredRail(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance, block) || instance.getBlock() instanceof PoweredRailBlock;
    }

    @WrapOperation(method = "isSameRailWithPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 0))
    public Comparable kilt$getRailDirectionWithForge(BlockState instance, Property property, Operation<Comparable> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), PoweredRailBlock.class, "getRailDirection", BlockState.class, BlockGetter.class, BlockPos.class, AbstractMinecart.class)) {
            return ((PoweredRailBlock) instance.getBlock()).getRailDirection(instance, level, pos, (AbstractMinecart) null);
        }

        return original.call(instance, property);
    }

    @Redirect(method = "isSameRailWithPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 1))
    public Comparable kilt$checkIfRailIsActivator(BlockState instance, Property property) {
        return isActivatorRail() == ((PoweredRailBlockInjection) instance.getBlock()).isActivatorRail();
    }

    @ModifyReceiver(method = "isSameRailWithPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/PoweredRailBlock;findPoweredRailSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZI)Z"))
    public PoweredRailBlock kilt$useOtherRailForSignal(PoweredRailBlock instance, Level level, BlockPos pos, BlockState state, boolean searchForward, int recursionCount) {
        return ((PoweredRailBlock) state.getBlock());
    }

    @ModifyArg(method = "updateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 1))
    public Property kilt$useForgeShapeForAscension(Property original) {
        if (original != SHAPE)
            return original;

        return getShapeProperty();
    }

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"), cancellable = true)
    public void kilt$useForgeShapeForDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(getShapeProperty(), POWERED, WATERLOGGED);

        ci.cancel();
    }
}