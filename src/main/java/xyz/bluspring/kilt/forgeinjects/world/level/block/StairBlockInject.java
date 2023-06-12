package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.util.function.Supplier;

@Mixin(StairBlock.class)
public class StairBlockInject extends Block {
    @Shadow @Final public static DirectionProperty FACING;
    @Shadow @Final public static EnumProperty<Half> HALF;
    @Shadow @Final public static EnumProperty<StairsShape> SHAPE;
    @Shadow @Final public static BooleanProperty WATERLOGGED;
    @Shadow @Final @Mutable
    private Block base;
    @Shadow @Final @Mutable private BlockState baseState;
    private Supplier<BlockState> stateSupplier;

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("TAIL"))
    public void kilt$setBaseStateAsBlockState(BlockState blockState, Properties properties, CallbackInfo ci) {
        this.stateSupplier = () -> blockState;
    }

    @CreateInitializer
    public StairBlockInject(Supplier<BlockState> stateSupplier, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATERLOGGED, false));

        this.stateSupplier = stateSupplier;
        // this is not accurate to what Forge does, but I can't be bothered to coremod *or* ASM these.
        // if this bites me in the ass later, welp.
        this.base = this.getModelBlock();
        this.baseState = this.getModelState();
    }

    // i'm staring at this code, and i'm questioning.. why are these private?
    // it just keeps the block state being air, these aren't used anywhere in the Forge patches.
    // I'm keeping it private for the sake of matching with Forge, but what the hell??

    // 3 minutes later, i have an update:
    // they coremod it to redirect fields to this method.
    // why.
    private Block getModelBlock() {
        return this.getModelState().getBlock();
    }

    private BlockState getModelState() {
        return this.stateSupplier.get();
    }
}
