package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@IfModLoaded("registrate-fabric")
@Mixin(ForgeFlowingFluid.class)
public interface ForgeFlowingFluidAccessor {
    @Final @Accessor("fluidType") @Mutable
    void kilt$fluidType(Supplier<? extends FluidType> fluidType);
    @Final @Accessor("flowing") @Mutable
    void kilt$flowing(Supplier<? extends Fluid> flowing);
    @Final @Accessor("still") @Mutable
    void kilt$still(Supplier<? extends Fluid> still);
    @Final @Accessor("bucket") @Mutable
    void kilt$bucket(Supplier<? extends Item> bucket);
    @Final @Accessor("block") @Mutable
    void kilt$block(Supplier<? extends LiquidBlock> block);
    @Final @Accessor("slopeFindDistance") @Mutable
    void kilt$slopeFindDistance(int slopeFindDistance);
    @Final @Accessor("levelDecreasePerBlock") @Mutable
    void kilt$levelDecreasePerBlock(int levelDecreasePerBlock);
    @Final @Accessor("explosionResistance") @Mutable
    void kilt$explosionResistance(float explosionResistance);
    @Final @Accessor("tickRate") @Mutable
    void kilt$tickRate(int tickRate);

    @Accessor int getSlopeFindDistance();
    @Accessor int getLevelDecreasePerBlock();
    @Accessor float getExplosionResistance();
    @Accessor int getTickRate();
    @Accessor Supplier<? extends LiquidBlock> getBlock();

    @Invoker BlockState callCreateLegacyBlock(FluidState state);
    @Invoker float callGetExplosionResistance();
    @Invoker boolean callCanBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn, Direction direction);
    @Invoker int callGetDropOff(LevelReader worldIn);
    @Invoker int callGetSlopeFindDistance(LevelReader worldIn);
    @Invoker void callBeforeDestroyingBlock(LevelAccessor worldIn, BlockPos pos, BlockState state);
    @Invoker boolean callCanConvertToSource(Level level);
}
