package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.PoweredRailBlockInjection;

@Mixin(PoweredRailBlock.class)
public abstract class PoweredRailBlockInject extends BaseRailBlock implements PoweredRailBlockInjection {
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
    public Property<RailShape> kilt$useForgeShape(Property<RailShape> par1) {
        return getShapeProperty();
    }

    @Redirect(method = "isSameRailWithPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean kilt$checkIfBlockIsPoweredRail(BlockState instance, Block block) {
        return instance.getBlock() instanceof PoweredRailBlock;
    }

    @Redirect(method = "isSameRailWithPower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", ordinal = 0))
    public Comparable kilt$getRailDirectionWithForge(BlockState instance, Property property, Level level, BlockPos pos) {
        return ((PoweredRailBlock) instance.getBlock()).getRailDirection(instance, level, pos, null);
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
    public Property kilt$useForgeShapeForAscension(Property par1) {
        return getShapeProperty();
    }

    @ModifyArgs(method = "createBlockStateDefinition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/StateDefinition$Builder;add([Lnet/minecraft/world/level/block/state/properties/Property;)Lnet/minecraft/world/level/block/state/StateDefinition$Builder;"))
    public void kilt$useForgeShapeForDefinition(Args args) {
        args.set(0, getShapeProperty());
    }
}
