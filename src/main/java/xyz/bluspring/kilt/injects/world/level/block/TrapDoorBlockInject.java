package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TrapDoorBlock.class)
public abstract class TrapDoorBlockInject extends HorizontalDirectionalBlock {
    @Shadow @Final public static BooleanProperty OPEN;

    protected TrapDoorBlockInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        if (state.getValue(OPEN)) {
            var downPos = pos.below();
            var down = level.getBlockState(downPos);

            return down.getBlock().makesOpenTrapdoorAboveClimbable(down, level, downPos, state);
        }

        return false;
    }
}
