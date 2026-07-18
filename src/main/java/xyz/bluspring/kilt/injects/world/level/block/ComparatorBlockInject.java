package xyz.bluspring.kilt.injects.world.level.block;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockInject extends Block {
    public ComparatorBlockInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean getWeakChanges(BlockState state, LevelReader level, BlockPos pos) {
        return state.is(Blocks.COMPARATOR);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (pos.getY() == neighbor.getY() && level instanceof Level && !level.isClientSide()) {
            state.handleNeighborChanged((Level) level, pos, level.getBlockState(neighbor).getBlock(), null, false);
        }
    }
}
