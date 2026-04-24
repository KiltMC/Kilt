package xyz.bluspring.kilt.injects.world.level.block;

import net.neoforged.neoforge.fluids.CauldronFluidContent;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockInject extends Block {
    public AbstractCauldronBlockInject(Properties properties) {
        super(properties);
    }

    @Intrinsic
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (CauldronFluidContent.getForBlock(state.getBlock()) == null) {
            level.invalidateCapabilities(pos);
        }
    }

    @Intrinsic
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (CauldronFluidContent.getForBlock(state.getBlock()) == null) {
            level.invalidateCapabilities(pos);
        }
    }
}
