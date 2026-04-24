package xyz.bluspring.kilt.injects.world.level.block.entity;

import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityInject extends RandomizableContainerBlockEntity {
    protected ChestBlockEntityInject(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Intrinsic
    public void setBlockState(BlockState state) {
        var oldState = this.getBlockState();
        super.setBlockState(state);
        if ((oldState.getValue(ChestBlock.FACING) != state.getValue(ChestBlock.FACING)) || (oldState.getValue(ChestBlock.TYPE) != state.getValue(ChestBlock.TYPE))) {
            this.invalidateCapabilities();
        }
    }
}
