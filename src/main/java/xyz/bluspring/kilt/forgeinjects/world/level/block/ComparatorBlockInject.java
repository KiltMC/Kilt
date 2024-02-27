// TRACKED HASH: 9e59b2390ceaa36845fcee2d052fdfa057ad42ba
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlockInject extends DiodeBlock {
    protected ComparatorBlockInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean getWeakChanges(BlockState state, LevelReader level, BlockPos pos) {
        return state.is(Blocks.COMPARATOR);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (pos.getY() == neighbor.getY() && level instanceof Level && !level.isClientSide()) {
            state.neighborChanged((Level) level, pos, level.getBlockState(neighbor).getBlock(), neighbor, false);
        }
    }
}