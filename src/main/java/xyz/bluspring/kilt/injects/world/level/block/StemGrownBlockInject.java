// TRACKED HASH: 6e6543f0d40c5323467075cb708a586ece0be5f7
package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StemGrownBlock.class)
public abstract class StemGrownBlockInject extends Block implements IPlantable {
    public StemGrownBlockInject(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getPlant(BlockGetter level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getBlock() != this)
            return this.defaultBlockState();
        return state;
    }
}