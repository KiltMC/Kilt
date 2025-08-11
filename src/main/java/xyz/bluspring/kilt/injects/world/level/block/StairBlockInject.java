// TRACKED HASH: eab4aa77b994f9a00088ad36b92af4db566cd67f
package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import org.spongepowered.asm.mixin.*;
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
    private boolean kilt$isModded;

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("TAIL"))
    public void kilt$setBaseStateAsBlockState(BlockState blockState, Properties properties, CallbackInfo ci) {
        this.stateSupplier = () -> blockState;
        this.kilt$isModded = false;
    }

    @CreateInitializer
    public StairBlockInject(Supplier<BlockState> stateSupplier, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATERLOGGED, false));

        this.stateSupplier = stateSupplier;
        this.base = Blocks.AIR;
        this.baseState = Blocks.AIR.defaultBlockState();
        this.kilt$isModded = true;
    }

    @ModifyExpressionValue(method = {"animateTick", "destroy", "getExplosionResistance", "onPlace", "stepOn", "isRandomlyTicking", "randomTick", "tick", "wasExploded"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/StairBlock;base:Lnet/minecraft/world/level/block/Block;"))
    private Block kilt$getBase(Block original) {
        if (this.kilt$isModded) {
            return getModelBlock();
        }
        return original;
    }

    @ModifyExpressionValue(method = {"attack", "onPlace", "onRemove", "use"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/StairBlock;baseState:Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$getBaseState(BlockState original) {
        if (this.kilt$isModded) {
            return getModelState();
        }
        return original;
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