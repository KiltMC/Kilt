// TRACKED HASH: 645dd70b9be6b9d17ac805f69fccfcbf748b17c1
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBaseRailBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockInject implements IForgeBaseRailBlock {
    @Shadow @Final private boolean isStraight;

    @Override
    public boolean isFlexibleRail(BlockState state, BlockGetter level, BlockPos pos) {
        return !this.isStraight;
    }
}